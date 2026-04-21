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
package fr.paris.lutece.plugins.accountgenerator.web.rs;

import fr.paris.lutece.plugins.accountgenerator.business.AccountGenerationJob;
import fr.paris.lutece.plugins.accountgenerator.dto.AccountGenerationJobResponse;
import fr.paris.lutece.plugins.accountgenerator.service.AccountGenerationJobService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.account.generator.AccountGenerationRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestFormatException;
import fr.paris.lutece.plugins.rest.service.RestConstants;
import fr.paris.lutece.portal.service.progressmanager.ProgressManagerService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path( RestConstants.BASE_PATH + Constants.PLUGIN_PATH + Constants.VERSION_PATH_V3 + Constants.GENERATOR_PATH )
public class AccountGeneratorRestService
{
    private static final String JOB_PATH = "/job";

    @POST
    @Path( Constants.ACCOUNT_GENERATOR_PATH )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    public Response generateAccount( final AccountGenerationRequest accountGenerationRequest,
            @HeaderParam( Constants.PARAM_CLIENT_CODE ) String strHeaderClientCode, @HeaderParam( Constants.PARAM_AUTHOR_NAME ) String authorName,
            @HeaderParam( Constants.PARAM_AUTHOR_TYPE ) String authorType, @HeaderParam( Constants.PARAM_APPLICATION_CODE ) String strHeaderAppCode )
    {
        if ( accountGenerationRequest == null || accountGenerationRequest.getAccountGenerationDto( ) == null )
        {
            return badRequest( "The request must specify an account generation payload." );
        }
        try
        {
            final AccountGenerationJob job = AccountGenerationJobService.instance( ).submit( accountGenerationRequest.getAccountGenerationDto( ),
                    strHeaderClientCode, strHeaderAppCode, authorName, authorType, authorName );

            final AccountGenerationJobResponse response = new AccountGenerationJobResponse( );
            response.setReference( job.getReference( ) );
            response.setStatus( job.getStatus( ).name( ) );
            response.setCreationDate( job.getCreationDate( ) );
            response.setBatchSize( job.getBatchSize( ) );
            return Response.status( Response.Status.ACCEPTED ).entity( response ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
        }
        catch( final RequestFormatException e )
        {
            return badRequest( e.getMessage( ) );
        }
    }

    @GET
    @Path( Constants.ACCOUNT_GENERATOR_PATH + JOB_PATH + "/{reference}" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getJob( @PathParam( "reference" ) final String reference ) throws IdentityStoreException
    {
        final Optional<AccountGenerationJob> optJob = AccountGenerationJobService.instance( ).findByReference( reference );
        if ( !optJob.isPresent( ) )
        {
            return Response.status( Response.Status.NOT_FOUND ).entity( errorBody( "No job found for reference: " + reference ) )
                    .type( MediaType.APPLICATION_JSON_TYPE ).build( );
        }
        return Response.ok( toResponse( optJob.get( ) ), MediaType.APPLICATION_JSON_TYPE ).build( );
    }

    private static Response badRequest( final String message )
    {
        return Response.status( Response.Status.BAD_REQUEST ).entity( errorBody( message ) ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
    }

    private static Map<String, String> errorBody( final String message )
    {
        final Map<String, String> body = new HashMap<>( );
        body.put( "error", message );
        return body;
    }

    private static AccountGenerationJobResponse toResponse( final AccountGenerationJob job )
    {
        final AccountGenerationJobResponse response = new AccountGenerationJobResponse( );
        response.setReference( job.getReference( ) );
        response.setStatus( job.getStatus( ).name( ) );
        response.setCreationDate( job.getCreationDate( ) );
        response.setCompletionDate( job.getCompletionDate( ) );
        response.setBatchSize( job.getBatchSize( ) );
        response.setNbProcessed( job.getNbProcessed( ) );
        response.setNbSuccess( job.getNbSuccess( ) );
        response.setNbFailure( job.getNbFailure( ) );
        response.setErrorMessage( job.getErrorMessage( ) );

        final String feedToken = AccountGenerationJobService.instance( ).getProgressFeedToken( job.getReference( ) );
        if ( feedToken != null )
        {
            response.setProgressPercent( ProgressManagerService.getInstance( ).getProgressStatus( feedToken ) );
            response.setProgressReport( ProgressManagerService.getInstance( ).getReport( feedToken ) );
        }
        else if ( job.getBatchSize( ) > 0 )
        {
            response.setProgressPercent( (int) ( job.getNbProcessed( ) * 100.0 / job.getBatchSize( ) + 0.5 ) );
        }

        response.setDownloadUrl( AccountGenerationJobService.instance( ).getDownloadUrl( job ) );
        return response;
    }
}
