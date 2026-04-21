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
package fr.paris.lutece.plugins.accountgenerator.business;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;

import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccountGenerationJobDao implements IAccountGenerationJobDao
{
    private static final String COLUMNS = "id_job, reference, status, date_creation, date_completion, date_accounts_deletion, user_name, client_code, app_code, author_name, author_type, batch_size, nb_processed, nb_success, nb_failure, request_json, file_key, file_name, error_message";

    private static final String SQL_INSERT = "INSERT INTO accountgenerator_job ( reference, status, date_creation, date_completion, date_accounts_deletion, user_name, client_code, app_code, author_name, author_type, batch_size, nb_processed, nb_success, nb_failure, request_json, file_key, file_name, error_message ) VALUES ( ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,? )";
    private static final String SQL_UPDATE = "UPDATE accountgenerator_job SET status = ?, date_completion = ?, date_accounts_deletion = ?, nb_processed = ?, nb_success = ?, nb_failure = ?, file_key = ?, file_name = ?, error_message = ? WHERE id_job = ?";
    private static final String SQL_SELECT_BY_REF = "SELECT " + COLUMNS + " FROM accountgenerator_job WHERE reference = ?";
    private static final String SQL_SELECT_BY_ID = "SELECT " + COLUMNS + " FROM accountgenerator_job WHERE id_job = ?";
    private static final String SQL_SELECT_ALL = "SELECT " + COLUMNS + " FROM accountgenerator_job ORDER BY date_creation DESC, id_job DESC";

    @Override
    public void insert( final AccountGenerationJob job, final Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_INSERT, Statement.RETURN_GENERATED_KEYS, plugin ) )
        {
            int i = 0;
            daoUtil.setString( ++i, job.getReference( ) );
            daoUtil.setString( ++i, job.getStatus( ).name( ) );
            daoUtil.setTimestamp( ++i, job.getCreationDate( ) );
            daoUtil.setTimestamp( ++i, job.getCompletionDate( ) );
            daoUtil.setTimestamp( ++i, job.getAccountsDeletionDate( ) );
            daoUtil.setString( ++i, job.getUser( ) );
            daoUtil.setString( ++i, job.getClientCode( ) );
            daoUtil.setString( ++i, job.getAppCode( ) );
            daoUtil.setString( ++i, job.getAuthorName( ) );
            daoUtil.setString( ++i, job.getAuthorType( ) );
            daoUtil.setInt( ++i, job.getBatchSize( ) );
            daoUtil.setInt( ++i, job.getNbProcessed( ) );
            daoUtil.setInt( ++i, job.getNbSuccess( ) );
            daoUtil.setInt( ++i, job.getNbFailure( ) );
            daoUtil.setString( ++i, job.getRequestJson( ) );
            daoUtil.setString( ++i, job.getFileKey( ) );
            daoUtil.setString( ++i, job.getFileName( ) );
            daoUtil.setString( ++i, job.getErrorMessage( ) );
            daoUtil.executeUpdate( );
            if ( daoUtil.nextGeneratedKey( ) )
            {
                job.setId( daoUtil.getGeneratedKeyInt( 1 ) );
            }
        }
    }

    @Override
    public void update( final AccountGenerationJob job, final Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_UPDATE, plugin ) )
        {
            int i = 0;
            daoUtil.setString( ++i, job.getStatus( ).name( ) );
            daoUtil.setTimestamp( ++i, job.getCompletionDate( ) );
            daoUtil.setTimestamp( ++i, job.getAccountsDeletionDate( ) );
            daoUtil.setInt( ++i, job.getNbProcessed( ) );
            daoUtil.setInt( ++i, job.getNbSuccess( ) );
            daoUtil.setInt( ++i, job.getNbFailure( ) );
            daoUtil.setString( ++i, job.getFileKey( ) );
            daoUtil.setString( ++i, job.getFileName( ) );
            daoUtil.setString( ++i, job.getErrorMessage( ) );
            daoUtil.setInt( ++i, job.getId( ) );
            daoUtil.executeUpdate( );
        }
    }

    @Override
    public Optional<AccountGenerationJob> loadByReference( final String strReference, final Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_SELECT_BY_REF, plugin ) )
        {
            daoUtil.setString( 1, strReference );
            daoUtil.executeQuery( );
            if ( daoUtil.next( ) )
            {
                return Optional.of( fromRow( daoUtil ) );
            }
        }
        return Optional.empty( );
    }

    @Override
    public Optional<AccountGenerationJob> loadById( final int nId, final Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_SELECT_BY_ID, plugin ) )
        {
            daoUtil.setInt( 1, nId );
            daoUtil.executeQuery( );
            if ( daoUtil.next( ) )
            {
                return Optional.of( fromRow( daoUtil ) );
            }
        }
        return Optional.empty( );
    }

    @Override
    public List<AccountGenerationJob> loadAll( final Plugin plugin )
    {
        final List<AccountGenerationJob> list = new ArrayList<>( );
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_SELECT_ALL, plugin ) )
        {
            daoUtil.executeQuery( );
            while ( daoUtil.next( ) )
            {
                list.add( fromRow( daoUtil ) );
            }
        }
        return list;
    }

    private static AccountGenerationJob fromRow( final DAOUtil daoUtil )
    {
        final AccountGenerationJob job = new AccountGenerationJob( );
        int i = 0;
        job.setId( daoUtil.getInt( ++i ) );
        job.setReference( daoUtil.getString( ++i ) );
        job.setStatus( AccountGenerationJobStatus.valueOf( daoUtil.getString( ++i ) ) );
        job.setCreationDate( daoUtil.getTimestamp( ++i ) );
        final Timestamp completion = daoUtil.getTimestamp( ++i );
        job.setCompletionDate( completion );
        job.setAccountsDeletionDate( daoUtil.getTimestamp( ++i ) );
        job.setUser( daoUtil.getString( ++i ) );
        job.setClientCode( daoUtil.getString( ++i ) );
        job.setAppCode( daoUtil.getString( ++i ) );
        job.setAuthorName( daoUtil.getString( ++i ) );
        job.setAuthorType( daoUtil.getString( ++i ) );
        job.setBatchSize( daoUtil.getInt( ++i ) );
        job.setNbProcessed( daoUtil.getInt( ++i ) );
        job.setNbSuccess( daoUtil.getInt( ++i ) );
        job.setNbFailure( daoUtil.getInt( ++i ) );
        job.setRequestJson( daoUtil.getString( ++i ) );
        job.setFileKey( daoUtil.getString( ++i ) );
        job.setFileName( daoUtil.getString( ++i ) );
        job.setErrorMessage( daoUtil.getString( ++i ) );
        return job;
    }
}
