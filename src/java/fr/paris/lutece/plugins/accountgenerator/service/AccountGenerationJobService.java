/*
 * Copyright (c) 2002-2025, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.accountgenerator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.paris.lutece.plugins.accountgenerator.business.AccountGenerationJob;
import fr.paris.lutece.plugins.accountgenerator.business.AccountGenerationJobHome;
import fr.paris.lutece.plugins.accountgenerator.business.AccountGenerationJobStatus;
import fr.paris.lutece.plugins.accountgenerator.service.file.implementation.LocalFileSystemDirectoryFileService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.account.generator.AccountGenerationDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.account.generator.GeneratedAccountDto;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestFormatException;
import fr.paris.lutece.portal.service.file.FileService;
import fr.paris.lutece.portal.service.file.IFileStoreServiceProvider;
import fr.paris.lutece.portal.service.progressmanager.ProgressManagerService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * Orchestrates asynchronous account generation jobs: validation, persistence, progress tracking, CSV output, and download-link publication.
 */
public class AccountGenerationJobService
{
    public static final String FILE_PROVIDER_NAME = "accountgenerator.localFileSystemDirectoryFileService";

    private static final int FILE_FLUSH_SIZE = AppPropertiesService.getPropertyInt( "accountgenerator.generation.file.flush.size", 10 );
    private static final String CSV_SEPARATOR = AppPropertiesService.getProperty( "accountgenerator.generation.csv.separator", ";" );
    private static final String CSV_HEADER = "email" + CSV_SEPARATOR + "password" + CSV_SEPARATOR + "cuid" + CSV_SEPARATOR + "guid";

    private static AccountGenerationJobService _instance;

    private final ExecutorService _executor;
    private final ObjectMapper _objectMapper = new ObjectMapper( );
    // Progress feed tokens are transient; they live only as long as the running job. Map key: job reference.
    private final ConcurrentHashMap<String, String> _feedTokens = new ConcurrentHashMap<>( );

    public AccountGenerationJobService( final ExecutorService executor )
    {
        _executor = executor;
    }

    public static synchronized AccountGenerationJobService instance( )
    {
        if ( _instance == null )
        {
            _instance = SpringContextService.getBean( "accountgenerator.jobService" );
        }
        return _instance;
    }

    /**
     * Validate the request, persist a PENDING job and schedule its asynchronous execution. Throws {@link RequestFormatException} on invalid input (no DB
     * insert).
     */
    public AccountGenerationJob submit( final AccountGenerationDto dto, final String clientCode, final String appCode, final String authorName,
            final String authorType, final String user ) throws RequestFormatException
    {
        AccountGenerationJobValidationService.instance( ).validate( dto );

        final AccountGenerationJob job = new AccountGenerationJob( );
        job.setReference( UUID.randomUUID( ).toString( ) );
        job.setStatus( AccountGenerationJobStatus.PENDING );
        job.setCreationDate( Timestamp.from( Instant.now( ) ) );
        job.setUser( user );
        job.setClientCode( clientCode );
        job.setAppCode( appCode );
        job.setAuthorName( authorName );
        job.setAuthorType( authorType );
        job.setBatchSize( dto.getBatchSize( ) );
        job.setNbProcessed( 0 );
        job.setNbSuccess( 0 );
        job.setNbFailure( 0 );
        try
        {
            job.setRequestJson( _objectMapper.writeValueAsString( dto ) );
        }
        catch( final Exception e )
        {
            AppLogService.error( "Could not serialize AccountGenerationDto for job persistence", e );
        }

        AccountGenerationJobHome.create( job );

        final String feedToken = ProgressManagerService.getInstance( ).registerFeed( job.getReference( ), dto.getBatchSize( ) );
        _feedTokens.put( job.getReference( ), feedToken );

        _executor.submit( ( ) -> runJob( job, dto, feedToken ) );

        return job;
    }

    public Optional<AccountGenerationJob> findByReference( final String reference )
    {
        return AccountGenerationJobHome.findByReference( reference );
    }

    public List<AccountGenerationJob> findAll( )
    {
        return AccountGenerationJobHome.findAll( );
    }

    /**
     * @return the transient progress feed token for a running job, or {@code null} once the job is done (or unknown).
     */
    public String getProgressFeedToken( final String reference )
    {
        return _feedTokens.get( reference );
    }

    /**
     * Build a backoffice download URL for a completed job, or {@code null} if no file is available yet (or has been deleted).
     */
    public String getDownloadUrl( final AccountGenerationJob job )
    {
        if ( job == null || job.getFileKey( ) == null || job.getAccountsDeletionDate( ) != null )
        {
            return null;
        }
        return getFileStoreService( ).getFileDownloadUrlBO( job.getFileKey( ) );
    }

    /**
     * Return the first {@code maxLines} rows of the job's generated CSV (excluding the header), each row split on the configured separator. Empty list if the
     * file is missing.
     */
    public List<String [ ]> getCsvPreview( final AccountGenerationJob job, final int maxLines )
    {
        if ( job == null || job.getFileKey( ) == null || job.getAccountsDeletionDate( ) != null || maxLines <= 0 )
        {
            return java.util.Collections.emptyList( );
        }
        final LocalFileSystemDirectoryFileService localProvider = (LocalFileSystemDirectoryFileService) getFileStoreService( );
        final Path csvPath = localProvider.getStorageDir( ).toPath( ).resolve( job.getFileKey( ) );
        if ( !Files.exists( csvPath ) )
        {
            return java.util.Collections.emptyList( );
        }
        final List<String [ ]> rows = new java.util.ArrayList<>( );
        try ( BufferedReader reader = Files.newBufferedReader( csvPath, StandardCharsets.UTF_8 ) )
        {
            reader.readLine( ); // skip header
            String line;
            int count = 0;
            while ( count < maxLines && ( line = reader.readLine( ) ) != null )
            {
                rows.add( line.split( java.util.regex.Pattern.quote( CSV_SEPARATOR ), -1 ) );
                count++;
            }
        }
        catch( final IOException e )
        {
            AppLogService.error( "Failed to read CSV preview for job " + job.getReference( ), e );
        }
        return rows;
    }

    /**
     * Delete every identity-account generated by the given job from the external APIs (AccountManagement + IdentityStore) and from the local DB, then remove
     * the CSV file and mark the job with an accounts-deletion timestamp. Idempotent: calling it on a job whose accounts have already been deleted is a no-op.
     */
    public synchronized AccountGenerationJob deleteGeneratedAccounts( final String reference )
    {
        final Optional<AccountGenerationJob> optJob = AccountGenerationJobHome.findByReference( reference );
        if ( !optJob.isPresent( ) )
        {
            return null;
        }
        final AccountGenerationJob job = optJob.get( );
        if ( job.getAccountsDeletionDate( ) != null )
        {
            return job;
        }

        IdentityAccountPurgeService.instance( ).purgeByJobReference( reference );

        if ( job.getFileKey( ) != null )
        {
            final LocalFileSystemDirectoryFileService localProvider = (LocalFileSystemDirectoryFileService) getFileStoreService( );
            final Path csvPath = localProvider.getStorageDir( ).toPath( ).resolve( job.getFileKey( ) );
            try
            {
                Files.deleteIfExists( csvPath );
            }
            catch( final IOException e )
            {
                AppLogService.error( "Failed to delete CSV for job " + reference, e );
            }
        }

        job.setAccountsDeletionDate( Timestamp.from( Instant.now( ) ) );
        AccountGenerationJobHome.update( job );
        return job;
    }

    private void runJob( final AccountGenerationJob job, final AccountGenerationDto dto, final String feedToken )
    {
        final ProgressManagerService progress = ProgressManagerService.getInstance( );
        final IFileStoreServiceProvider fileStoreService = getFileStoreService( );
        final LocalFileSystemDirectoryFileService localProvider = (LocalFileSystemDirectoryFileService) fileStoreService;

        final String fileName = "accountgenerator-" + job.getReference( ) + ".csv";
        final Path csvPath = localProvider.getStorageDir( ).toPath( ).resolve( fileName );

        job.setStatus( AccountGenerationJobStatus.IN_PROGRESS );
        AccountGenerationJobHome.update( job );
        progress.addReport( feedToken, "Job " + job.getReference( ) + " started (batch size: " + dto.getBatchSize( ) + ")" );

        BufferedWriter writer = null;
        try
        {
            Files.createDirectories( csvPath.getParent( ) );
            writer = Files.newBufferedWriter( csvPath, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING );
            writer.write( CSV_HEADER );
            writer.newLine( );

            final int [ ] successCount = { 0 };
            final int [ ] failureCount = { 0 };
            final int [ ] lastFlushIndex = { 0 };
            final BufferedWriter writerRef = writer;

            IdentityAccountGeneratorService.instance( ).createIdentityAccountBatch( dto, job.getReference( ), ( account, index ) -> {
                try
                {
                    writerRef.write( csvLine( account ) );
                    writerRef.newLine( );
                }
                catch( final IOException ioe )
                {
                    AppLogService.error( "Failed to write account to CSV for job " + job.getReference( ), ioe );
                }

                final boolean success = account.getCuid( ) != null || account.getGuid( ) != null;
                if ( success )
                {
                    successCount[0]++;
                    progress.incrementSuccess( feedToken, 1 );
                }
                else
                {
                    failureCount[0]++;
                    progress.incrementFailure( feedToken, 1 );
                }

                if ( index - lastFlushIndex[0] >= FILE_FLUSH_SIZE || index == dto.getBatchSize( ) )
                {
                    try
                    {
                        writerRef.flush( );
                    }
                    catch( final IOException ioe )
                    {
                        AppLogService.error( "Failed to flush CSV for job " + job.getReference( ), ioe );
                    }
                    progress.addReport( feedToken, "Processed " + index + " / " + dto.getBatchSize( ) );
                    lastFlushIndex[0] = index;
                }
            } );

            writer.flush( );
            writer.close( );
            writer = null;

            job.setStatus( AccountGenerationJobStatus.COMPLETED );
            job.setCompletionDate( Timestamp.from( Instant.now( ) ) );
            job.setNbProcessed( successCount[0] + failureCount[0] );
            job.setNbSuccess( successCount[0] );
            job.setNbFailure( failureCount[0] );
            job.setFileKey( fileName );
            job.setFileName( fileName );
            AccountGenerationJobHome.update( job );
            progress.addReport( feedToken, "Job completed: " + successCount[0] + " success, " + failureCount[0] + " failure" );
        }
        catch( final Exception e )
        {
            AppLogService.error( "Job " + job.getReference( ) + " failed", e );
            job.setStatus( AccountGenerationJobStatus.FAILED );
            job.setCompletionDate( Timestamp.from( Instant.now( ) ) );
            job.setErrorMessage( e.getClass( ).getSimpleName( ) + ": " + e.getMessage( ) );
            AccountGenerationJobHome.update( job );
            progress.addReport( feedToken, "Job failed: " + e.getMessage( ) );
        }
        finally
        {
            if ( writer != null )
            {
                try
                {
                    writer.close( );
                }
                catch( final IOException ignore )
                {
                }
            }
            _feedTokens.remove( job.getReference( ) );
            progress.unRegisterFeed( feedToken );
        }
    }

    private static String csvLine( final GeneratedAccountDto account )
    {
        return safe( account.getEmail( ) ) + CSV_SEPARATOR + safe( account.getPassword( ) ) + CSV_SEPARATOR + safe( account.getCuid( ) ) + CSV_SEPARATOR
                + safe( account.getGuid( ) );
    }

    private static String safe( final String value )
    {
        if ( value == null )
        {
            return "";
        }
        return value.replace( CSV_SEPARATOR, " " ).replace( "\n", " " ).replace( "\r", " " );
    }

    private static IFileStoreServiceProvider getFileStoreService( )
    {
        return FileService.getInstance( ).getFileStoreServiceProvider( FILE_PROVIDER_NAME );
    }
}
