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

import fr.paris.lutece.plugins.accountgenerator.business.IdentityAccount;
import fr.paris.lutece.plugins.accountgenerator.business.IdentityAccountHome;
import fr.paris.lutece.plugins.accountgenerator.dto.PurgeExipratedIdentityAccountsResult;
import fr.paris.lutece.plugins.accountmanagement.web.service.AccountManagementService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.account.ChangeAccountResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.account.RequestClient;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AuthorType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.RequestAuthor;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.service.IdentityService;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityAccountException;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import java.util.List;
import java.util.Objects;

public class IdentityAccountPurgeService
{
    // Singleton
    private static IdentityAccountPurgeService instance;

    public static IdentityAccountPurgeService instance( )
    {
        if ( instance == null )
        {
            instance = new IdentityAccountPurgeService( );
        }
        return instance;
    }

    // Configurable client generation information
    private static final String accountManagementClientId = AppPropertiesService.getProperty( "accountgenerator.accountManagement.client-id" );
    private static final String accountManagementSecretId = AppPropertiesService.getProperty( "accountgenerator.accountManagement.secret-id" );
    private static final String identityStoreClientCode = AppPropertiesService.getProperty( "accountgenerator.accountManagement.client-code" );
    private static final String identityStoreClientName = AppPropertiesService.getProperty( "accountgenerator.accountManagement.client-name" );

    // API client services
    private static final IdentityService _identityService = SpringContextService.getBean( "accountgenerator.identityService" );
    private static final AccountManagementService _accountManagementService = SpringContextService.getBean( "accountgenerator.accountManagementService" );

    // Class parameters
    private final RequestClient client;
    private final RequestAuthor author;

    // Constructor is private so that only singleton pattern can use it
    private IdentityAccountPurgeService( )
    {
        client = new RequestClient( );
        client.setClientId( accountManagementClientId );
        client.setClientSecret( accountManagementSecretId );

        author = new RequestAuthor( );
        author.setName( identityStoreClientName );
        author.setType( AuthorType.application );
    }

    public PurgeExipratedIdentityAccountsResult purge( )
    {
        final PurgeExipratedIdentityAccountsResult result = new PurgeExipratedIdentityAccountsResult( );
        final List<IdentityAccount> accounts = IdentityAccountHome.loadExpiredAccounts( );
        for ( final IdentityAccount account : accounts )
        {
            // Hold errors in order to clean data if both identity and account cannot be generated
            boolean accountError = false;

            if ( account.getGuid( ) != null )
            {
                try
                {
                    final ChangeAccountResponse changeAccountResponse = _accountManagementService.deleteAccount( account.getGuid( ), client );
                    AppLogService.debug( "Tried to delete account " + account.getGuid( ) + " : " + changeAccountResponse );
                    if ( Objects.equals( changeAccountResponse.getStatus( ), "OK" ) )
                    {
                        result.incrementNbDeletedAccounts( );
                    }
                }
                catch( final IdentityAccountException e )
                {
                    AppLogService.info( "An exception occurred when trying to delete the account " + account.getGuid( ) + " : " + e.getMessage( ) );
                    accountError = true;
                }
            }
            else
            {
                AppLogService.debug( "No account to be deleted" );
            }

            if ( !accountError && account.getCuid( ) != null )
            {
                try
                {
                    final IdentityChangeResponse deletedIdentityResponse = _identityService.deleteIdentity( account.getGuid( ), identityStoreClientCode,
                            author );
                    AppLogService.debug( "Tried to delete identity " + account.getCuid( ) + " : " + deletedIdentityResponse );
                    if ( deletedIdentityResponse.getStatus( ).getHttpCode( ) == 200 || deletedIdentityResponse.getStatus( ).getHttpCode( ) == 201 )
                    {
                        result.incrementNbDeletedIdentities( );
                    }
                }
                catch( final IdentityStoreException e )
                {
                    AppLogService.info( "An exception occurred when trying to delete the identity " + account.getCuid( ) + " : " + e.getMessage( ) );
                }
            }
        }
        return result;
    }

}
