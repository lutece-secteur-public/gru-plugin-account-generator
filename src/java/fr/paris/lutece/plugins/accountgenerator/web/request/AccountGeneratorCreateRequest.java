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
package fr.paris.lutece.plugins.accountgenerator.web.request;

import fr.paris.lutece.plugins.accountgenerator.service.IdentityAccountGeneratorService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.AbstractIdentityStoreRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.account.generator.AccountGenerationDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.account.generator.AccountGenerationRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.account.generator.AccountGenerationResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.account.generator.GeneratedAccountDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.ResponseStatusFactory;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import java.util.List;

public class AccountGeneratorCreateRequest extends AbstractIdentityStoreRequest
{
    private static final int generationLimit = AppPropertiesService.getPropertyInt( "accountgenerator.generation.limit", 50 );
    private static String JSON_EXAMPLE = "{\n" + "    \"generation\": {\n" + "        \"generateAccount\": true,\n"
            + "        \"generationPattern\": \"XYZ\",\n" + "        \"generationIncrementOffset\": 30,\n" + "        \"nbDaysOfValidity\": 300,\n"
            + "        \"batchSize\": 50\n" + "    }\n" + "}";
    private final AccountGenerationRequest _accountGenerationRequest;

    public AccountGeneratorCreateRequest( AccountGenerationRequest request, String strClientAppCode, String authorName, String authorType )
            throws IdentityStoreException
    {
        super( strClientAppCode, authorName, authorType );
        _accountGenerationRequest = request;
    }

    @Override
    protected void validateSpecificRequest( ) throws IdentityStoreException
    {
        if ( _accountGenerationRequest == null || _accountGenerationRequest.getAccountGenerationDto( ) == null )
        {
            throw new IdentityStoreException( "The request must specify an account generation. I.e: " + JSON_EXAMPLE );
        }
    }

    @Override
    protected AccountGenerationResponse doSpecificRequest( ) throws IdentityStoreException
    {
        final AccountGenerationResponse response = new AccountGenerationResponse( );
        final AccountGenerationDto accountGenerationDto = _accountGenerationRequest.getAccountGenerationDto( );
        String message = "";
        final int batchSize = Math.min( accountGenerationDto.getBatchSize( ), generationLimit );
        if ( batchSize != accountGenerationDto.getBatchSize( ) )
        {
            accountGenerationDto.setBatchSize( batchSize );
            message = "The number of accounts that can be generated in a single request is limited to " + generationLimit
                    + ", so the batch size has been override to this value.";
        }

        final List<GeneratedAccountDto> generatedAccounts = IdentityAccountGeneratorService.instance( ).createIdentityAccountBatch( accountGenerationDto );
        response.setGeneratedAccountList( generatedAccounts );
        response.setStatus( ResponseStatusFactory.success( ) );
        final long accountsCount = generatedAccounts.stream( ).filter( GeneratedAccountDto::hasAccount ).count( );
        final long identitiesCount = generatedAccounts.stream( ).filter( GeneratedAccountDto::hasIdentity ).count( );
        response.getStatus( ).setMessage(
                message + " Generated " + accountsCount + " accounts and/or " + identitiesCount + " identities. See each account status if some are missing." );
        return response;
    }
}
