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
package fr.paris.lutece.plugins.accountgenerator.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;
import java.util.List;

@JsonInclude( JsonInclude.Include.NON_NULL )
public class AccountGenerationJobResponse
{
    @JsonProperty( "reference" )
    private String reference;

    @JsonProperty( "status" )
    private String status;

    @JsonProperty( "creation_date" )
    private Timestamp creationDate;

    @JsonProperty( "completion_date" )
    private Timestamp completionDate;

    @JsonProperty( "batch_size" )
    private int batchSize;

    @JsonProperty( "nb_processed" )
    private int nbProcessed;

    @JsonProperty( "nb_success" )
    private int nbSuccess;

    @JsonProperty( "nb_failure" )
    private int nbFailure;

    @JsonProperty( "progress_percent" )
    private int progressPercent;

    @JsonProperty( "progress_report" )
    private List<String> progressReport;

    @JsonProperty( "download_url" )
    private String downloadUrl;

    @JsonProperty( "error_message" )
    private String errorMessage;

    public String getReference( )
    {
        return reference;
    }

    public void setReference( String reference )
    {
        this.reference = reference;
    }

    public String getStatus( )
    {
        return status;
    }

    public void setStatus( String status )
    {
        this.status = status;
    }

    public Timestamp getCreationDate( )
    {
        return creationDate;
    }

    public void setCreationDate( Timestamp creationDate )
    {
        this.creationDate = creationDate;
    }

    public Timestamp getCompletionDate( )
    {
        return completionDate;
    }

    public void setCompletionDate( Timestamp completionDate )
    {
        this.completionDate = completionDate;
    }

    public int getBatchSize( )
    {
        return batchSize;
    }

    public void setBatchSize( int batchSize )
    {
        this.batchSize = batchSize;
    }

    public int getNbProcessed( )
    {
        return nbProcessed;
    }

    public void setNbProcessed( int nbProcessed )
    {
        this.nbProcessed = nbProcessed;
    }

    public int getNbSuccess( )
    {
        return nbSuccess;
    }

    public void setNbSuccess( int nbSuccess )
    {
        this.nbSuccess = nbSuccess;
    }

    public int getNbFailure( )
    {
        return nbFailure;
    }

    public void setNbFailure( int nbFailure )
    {
        this.nbFailure = nbFailure;
    }

    public int getProgressPercent( )
    {
        return progressPercent;
    }

    public void setProgressPercent( int progressPercent )
    {
        this.progressPercent = progressPercent;
    }

    public List<String> getProgressReport( )
    {
        return progressReport;
    }

    public void setProgressReport( List<String> progressReport )
    {
        this.progressReport = progressReport;
    }

    public String getDownloadUrl( )
    {
        return downloadUrl;
    }

    public void setDownloadUrl( String downloadUrl )
    {
        this.downloadUrl = downloadUrl;
    }

    public String getErrorMessage( )
    {
        return errorMessage;
    }

    public void setErrorMessage( String errorMessage )
    {
        this.errorMessage = errorMessage;
    }
}
