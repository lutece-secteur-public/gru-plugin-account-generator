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

import java.sql.Timestamp;

public class AccountGenerationJob
{
    private int _nId;
    private String _strReference;
    private AccountGenerationJobStatus _status;
    private Timestamp _dateCreation;
    private Timestamp _dateCompletion;
    private Timestamp _dateAccountsDeletion;
    private String _strUser;
    private String _strClientCode;
    private String _strAppCode;
    private String _strAuthorName;
    private String _strAuthorType;
    private int _nBatchSize;
    private int _nNbProcessed;
    private int _nNbSuccess;
    private int _nNbFailure;
    private String _strRequestJson;
    private String _strFileKey;
    private String _strFileName;
    private String _strErrorMessage;

    public int getId( )
    {
        return _nId;
    }

    public void setId( int nId )
    {
        _nId = nId;
    }

    public String getReference( )
    {
        return _strReference;
    }

    public void setReference( String strReference )
    {
        _strReference = strReference;
    }

    public AccountGenerationJobStatus getStatus( )
    {
        return _status;
    }

    public void setStatus( AccountGenerationJobStatus status )
    {
        _status = status;
    }

    public Timestamp getCreationDate( )
    {
        return _dateCreation;
    }

    public void setCreationDate( Timestamp dateCreation )
    {
        _dateCreation = dateCreation;
    }

    public Timestamp getCompletionDate( )
    {
        return _dateCompletion;
    }

    public void setCompletionDate( Timestamp dateCompletion )
    {
        _dateCompletion = dateCompletion;
    }

    public Timestamp getAccountsDeletionDate( )
    {
        return _dateAccountsDeletion;
    }

    public void setAccountsDeletionDate( Timestamp dateAccountsDeletion )
    {
        _dateAccountsDeletion = dateAccountsDeletion;
    }

    public String getUser( )
    {
        return _strUser;
    }

    public void setUser( String strUser )
    {
        _strUser = strUser;
    }

    public String getClientCode( )
    {
        return _strClientCode;
    }

    public void setClientCode( String strClientCode )
    {
        _strClientCode = strClientCode;
    }

    public String getAppCode( )
    {
        return _strAppCode;
    }

    public void setAppCode( String strAppCode )
    {
        _strAppCode = strAppCode;
    }

    public String getAuthorName( )
    {
        return _strAuthorName;
    }

    public void setAuthorName( String strAuthorName )
    {
        _strAuthorName = strAuthorName;
    }

    public String getAuthorType( )
    {
        return _strAuthorType;
    }

    public void setAuthorType( String strAuthorType )
    {
        _strAuthorType = strAuthorType;
    }

    public int getBatchSize( )
    {
        return _nBatchSize;
    }

    public void setBatchSize( int nBatchSize )
    {
        _nBatchSize = nBatchSize;
    }

    public int getNbProcessed( )
    {
        return _nNbProcessed;
    }

    public void setNbProcessed( int nNbProcessed )
    {
        _nNbProcessed = nNbProcessed;
    }

    public int getNbSuccess( )
    {
        return _nNbSuccess;
    }

    public void setNbSuccess( int nNbSuccess )
    {
        _nNbSuccess = nNbSuccess;
    }

    public int getNbFailure( )
    {
        return _nNbFailure;
    }

    public void setNbFailure( int nNbFailure )
    {
        _nNbFailure = nNbFailure;
    }

    public String getRequestJson( )
    {
        return _strRequestJson;
    }

    public void setRequestJson( String strRequestJson )
    {
        _strRequestJson = strRequestJson;
    }

    public String getFileKey( )
    {
        return _strFileKey;
    }

    public void setFileKey( String strFileKey )
    {
        _strFileKey = strFileKey;
    }

    public String getFileName( )
    {
        return _strFileName;
    }

    public void setFileName( String strFileName )
    {
        _strFileName = strFileName;
    }

    public String getErrorMessage( )
    {
        return _strErrorMessage;
    }

    public void setErrorMessage( String strErrorMessage )
    {
        _strErrorMessage = strErrorMessage;
    }
}
