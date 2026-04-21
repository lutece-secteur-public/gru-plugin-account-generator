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

import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.account.generator.AccountGenerationDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestFormatException;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Validates an {@link AccountGenerationDto} before a job is persisted or launched. Throws {@link RequestFormatException} on the first invalid parameter so no DB
 * row is created.
 */
public final class AccountGenerationJobValidationService
{
    private static AccountGenerationJobValidationService _instance;

    private static final int MAX_BATCH_SIZE = AppPropertiesService.getPropertyInt( "accountgenerator.generation.limit", 100000 );
    private static final DateTimeFormatter BIRTHDATE_FORMAT = DateTimeFormatter.ofPattern( "dd/MM/yyyy" );

    private AccountGenerationJobValidationService( )
    {
    }

    public static synchronized AccountGenerationJobValidationService instance( )
    {
        if ( _instance == null )
        {
            _instance = new AccountGenerationJobValidationService( );
        }
        return _instance;
    }

    public void validate( final AccountGenerationDto dto ) throws RequestFormatException
    {
        if ( dto == null )
        {
            throw new RequestFormatException( "The request must specify an account generation payload.",
                    Constants.PROPERTY_REST_ERROR_ACCOUNT_GENERATION_REQUEST_FORMAT );
        }

        if ( dto.getBatchSize( ) <= 0 )
        {
            throw new RequestFormatException( "The batch size must be strictly positive.",
                    Constants.PROPERTY_REST_ERROR_ACCOUNT_GENERATION_REQUEST_FORMAT );
        }
        if ( dto.getBatchSize( ) > MAX_BATCH_SIZE )
        {
            throw new RequestFormatException( "The batch size cannot exceed " + MAX_BATCH_SIZE + ".",
                    Constants.PROPERTY_REST_ERROR_ACCOUNT_GENERATION_REQUEST_FORMAT );
        }
        if ( dto.getNbDaysOfValidity( ) <= 0 )
        {
            throw new RequestFormatException( "The number of days of validity must be strictly positive.",
                    Constants.PROPERTY_REST_ERROR_ACCOUNT_GENERATION_REQUEST_FORMAT );
        }
        if ( dto.getGenerationIncrementOffset( ) < 0 )
        {
            throw new RequestFormatException( "The generation increment offset must be zero or positive.",
                    Constants.PROPERTY_REST_ERROR_ACCOUNT_GENERATION_REQUEST_FORMAT );
        }

        final boolean hasPrefix = isNotBlank( dto.getLoginPrefix( ) );
        final boolean hasSuffix = isNotBlank( dto.getLoginSuffix( ) );
        if ( hasPrefix != hasSuffix )
        {
            throw new RequestFormatException( "loginPrefix and loginSuffix must both be provided or both be empty.",
                    Constants.PROPERTY_REST_ERROR_ACCOUNT_GENERATION_REQUEST_FORMAT );
        }
        if ( !hasPrefix && !hasSuffix && !isNotBlank( dto.getGenerationPattern( ) ) )
        {
            throw new RequestFormatException( "Either (loginPrefix + loginSuffix) or generationPattern must be provided.",
                    Constants.PROPERTY_REST_ERROR_ACCOUNT_GENERATION_REQUEST_FORMAT );
        }

        if ( isNotBlank( dto.getBirthdate( ) ) )
        {
            try
            {
                LocalDate.parse( dto.getBirthdate( ).trim( ), BIRTHDATE_FORMAT );
            }
            catch( final DateTimeParseException e )
            {
                throw new RequestFormatException( "The birthdate must follow the format dd/MM/yyyy.",
                        Constants.PROPERTY_REST_ERROR_ACCOUNT_GENERATION_REQUEST_FORMAT );
            }
        }
    }

    private static boolean isNotBlank( final String value )
    {
        return value != null && !value.trim( ).isEmpty( );
    }
}
