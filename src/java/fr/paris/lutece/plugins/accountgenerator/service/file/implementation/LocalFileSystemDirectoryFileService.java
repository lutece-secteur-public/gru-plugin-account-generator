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
package fr.paris.lutece.plugins.accountgenerator.service.file.implementation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import fr.paris.lutece.api.user.User;
import fr.paris.lutece.portal.business.file.File;
import fr.paris.lutece.portal.business.physicalfile.PhysicalFile;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.admin.AdminAuthenticationService;
import fr.paris.lutece.portal.service.file.ExpiredLinkException;
import fr.paris.lutece.portal.service.file.FileService;
import fr.paris.lutece.portal.service.file.IFileDownloadUrlService;
import fr.paris.lutece.portal.service.file.IFileRBACService;
import fr.paris.lutece.portal.service.file.IFileStoreServiceProvider;
import fr.paris.lutece.portal.service.security.SecurityService;
import fr.paris.lutece.portal.service.security.UserNotSignedException;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;

/**
 * Local file-system based implementation of IFileStoreServiceProvider. Stores files directly under a configured directory on disk, using the file's title as
 * the storage key. Copied from plugin-identityexport.
 */
public class LocalFileSystemDirectoryFileService implements IFileStoreServiceProvider
{
    private static final long serialVersionUID = 1L;

    private IFileDownloadUrlService _fileDownloadUrlService;
    private IFileRBACService _fileRBACService;
    private String _strName;
    private boolean _bDefault;
    private final java.io.File _storageDir;

    public LocalFileSystemDirectoryFileService( String strPath, IFileDownloadUrlService fileDownloadUrlService, IFileRBACService fileRBACService )
    {
        this._storageDir = new java.io.File( strPath );
        if ( !this._storageDir.exists( ) )
        {
            this._storageDir.mkdirs( );
        }
        this._fileDownloadUrlService = fileDownloadUrlService;
        this._fileRBACService = fileRBACService;
    }

    public IFileRBACService getFileRBACService( )
    {
        return _fileRBACService;
    }

    public void setFileRBACService( IFileRBACService fileRBACService )
    {
        this._fileRBACService = fileRBACService;
    }

    public IFileDownloadUrlService getDownloadUrlService( )
    {
        return _fileDownloadUrlService;
    }

    public void setDownloadUrlService( IFileDownloadUrlService downloadUrlService )
    {
        _fileDownloadUrlService = downloadUrlService;
    }

    public java.io.File getStorageDir( )
    {
        return _storageDir;
    }

    @Override
    public String getName( )
    {
        return _strName;
    }

    @Override
    public void delete( String strKey )
    {
        final java.io.File resource = new java.io.File( _storageDir, strKey );
        if ( resource.exists( ) )
        {
            resource.delete( );
        }
    }

    @Override
    public File getFile( String strKey )
    {
        return getFile( strKey, true );
    }

    @Override
    public File getFileMetaData( String strKey )
    {
        return getFile( strKey, false );
    }

    public File getFile( String strKey, boolean withPhysicalFile )
    {
        if ( StringUtils.isBlank( strKey ) || strKey.contains( "/" ) || strKey.contains( "\\" ) || strKey.contains( ".." ) )
        {
            return null;
        }

        final java.io.File resource = new java.io.File( _storageDir, strKey );
        if ( !resource.exists( ) )
        {
            return null;
        }

        final File file = new File( );
        file.setFileKey( strKey );
        file.setTitle( strKey );
        file.setOrigin( this.getName( ) );
        file.setSize( (int) resource.length( ) );

        try
        {
            file.setMimeType( Files.probeContentType( resource.toPath( ) ) );
        }
        catch( IOException e )
        {
            AppLogService.error( "unable to get MimeType of file", e );
        }

        if ( withPhysicalFile )
        {
            final PhysicalFile physicalFile = new PhysicalFile( );
            try
            {
                physicalFile.setValue( Files.readAllBytes( resource.toPath( ) ) );
                file.setPhysicalFile( physicalFile );
            }
            catch( IOException e )
            {
                AppLogService.error( e );
            }
        }

        return file;
    }

    @Override
    public String storeBytes( byte [ ] blob )
    {
        final String randomFileName = UUID.randomUUID( ).toString( );
        final java.io.File resource = new java.io.File( _storageDir, randomFileName );
        final Path path = Paths.get( resource.getAbsolutePath( ) );
        try
        {
            Files.write( path, blob );
        }
        catch( IOException e )
        {
            AppLogService.error( e );
        }
        return randomFileName;
    }

    @Override
    public String storeInputStream( InputStream inputStream )
    {
        return "method not implemented yet";
    }

    @Override
    public String storeFileItem( FileItem fileItem )
    {
        final java.io.File file = new java.io.File( _storageDir, FilenameUtils.getName( fileItem.getName( ) ) );
        final byte [ ] byteArray;
        try
        {
            byteArray = IOUtils.toByteArray( fileItem.getInputStream( ) );
        }
        catch( IOException ex )
        {
            throw new AppException( ex.getMessage( ), ex );
        }
        try
        {
            Files.write( Paths.get( file.getAbsolutePath( ) ), byteArray );
        }
        catch( IOException e )
        {
            AppLogService.error( e );
        }
        return fileItem.getName( );
    }

    @Override
    public String storeFile( File file )
    {
        final java.io.File resource = new java.io.File( _storageDir, file.getTitle( ) );
        final Path path = Paths.get( resource.getAbsolutePath( ) );
        try
        {
            Files.write( path, file.getPhysicalFile( ).getValue( ) );
        }
        catch( IOException e )
        {
            AppLogService.error( e );
        }
        return file.getTitle( );
    }

    public void setDefault( boolean bDefault )
    {
        this._bDefault = bDefault;
    }

    public void setName( String strName )
    {
        _strName = strName;
    }

    @Override
    public boolean isDefault( )
    {
        return _bDefault;
    }

    @Override
    public InputStream getInputStream( String strKey )
    {
        final File file = getFile( strKey );
        return new ByteArrayInputStream( file.getPhysicalFile( ).getValue( ) );
    }

    @Override
    public String getFileDownloadUrlFO( String strKey )
    {
        return _fileDownloadUrlService.getFileDownloadUrlFO( strKey, getName( ) );
    }

    @Override
    public String getFileDownloadUrlFO( String strKey, Map<String, String> additionnalData )
    {
        return _fileDownloadUrlService.getFileDownloadUrlFO( strKey, additionnalData, getName( ) );
    }

    @Override
    public String getFileDownloadUrlBO( String strKey )
    {
        return _fileDownloadUrlService.getFileDownloadUrlBO( strKey, getName( ) );
    }

    @Override
    public String getFileDownloadUrlBO( String strKey, Map<String, String> additionnalData )
    {
        return _fileDownloadUrlService.getFileDownloadUrlBO( strKey, additionnalData, getName( ) );
    }

    @Override
    public void checkAccessRights( Map<String, String> fileData, User user ) throws AccessDeniedException, UserNotSignedException
    {
        if ( _fileRBACService != null )
        {
            _fileRBACService.checkAccessRights( fileData, user );
        }
    }

    @Override
    public void checkLinkValidity( Map<String, String> fileData ) throws ExpiredLinkException
    {
        _fileDownloadUrlService.checkLinkValidity( fileData );
    }

    @Override
    public File getFileFromRequestBO( HttpServletRequest request ) throws AccessDeniedException, ExpiredLinkException, UserNotSignedException
    {
        final Map<String, String> fileData = _fileDownloadUrlService.getRequestDataBO( request );
        checkAccessRights( fileData, AdminAuthenticationService.getInstance( ).getRegisteredUser( request ) );
        checkLinkValidity( fileData );
        final String strFileId = fileData.get( FileService.PARAMETER_FILE_ID );
        return getFile( strFileId );
    }

    @Override
    public File getFileFromRequestFO( HttpServletRequest request ) throws AccessDeniedException, ExpiredLinkException, UserNotSignedException
    {
        final Map<String, String> fileData = _fileDownloadUrlService.getRequestDataFO( request );
        checkAccessRights( fileData, SecurityService.getInstance( ).getRegisteredUser( request ) );
        checkLinkValidity( fileData );
        final String strFileId = fileData.get( FileService.PARAMETER_FILE_ID );
        return getFile( strFileId );
    }
}
