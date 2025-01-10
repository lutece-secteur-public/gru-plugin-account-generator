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
import fr.paris.lutece.plugins.accountmanagement.web.service.AccountManagementService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.account.AccountDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.account.ChangeAccountResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.account.GetAccountResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.account.RequestClient;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.account.generator.AccountGenerationDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.account.generator.GeneratedAccountDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AuthorType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ExpirationDefinition;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.RequestAuthor;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.ResponseStatusFactory;
import fr.paris.lutece.plugins.identitystore.v3.web.service.IdentityService;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityAccountException;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class IdentityAccountGeneratorService
{
    // Singleton
    private static IdentityAccountGeneratorService instance;

    public static IdentityAccountGeneratorService instance( )
    {
        if ( instance == null )
        {
            instance = new IdentityAccountGeneratorService( );
        }
        return instance;
    }

    // API client services
    private static final IdentityService _identityService = SpringContextService.getBean( "accountgenerator.identityService" );
    private static final AccountManagementService _accountManagementService = SpringContextService.getBean( "accountgenerator.accountManagementService" );
    private static final GeocodesCache _geocodesCache = SpringContextService.getBean( "accountgenerator.geocodesCache" );

    // Configurable client generation information
    private static final String accountManagementClientId = AppPropertiesService.getProperty( "accountgenerator.accountManagement.client-id" );
    private static final String accountManagementSecretId = AppPropertiesService.getProperty( "accountgenerator.accountManagement.secret-id" );
    private static final String identityStoreClientCode = AppPropertiesService.getProperty( "accountgenerator.accountManagement.client-code" );
    private static final String identityStoreClientName = AppPropertiesService.getProperty( "accountgenerator.accountManagement.client-name" );

    // Configurable generation parameters
    private static final String commonPassword = AppPropertiesService.getProperty( "accountgenerator.generation.password", "password123456789" );
    private static final String commonMailSuffix = AppPropertiesService.getProperty( "accountgenerator.generation.mail.suffix", "@paris.test.fr" );
    private static final String mailCertifier = AppPropertiesService.getProperty( "accountgenerator.generation.certifier.email", "MAIL" );
    private static final String loginCertifier = AppPropertiesService.getProperty( "accountgenerator.generation.certifier.login", "MAIL" );
    private static final String firstNameCertifier = AppPropertiesService.getProperty( "accountgenerator.generation.certifier.first_name", "FC" );
    private static final String familyNameCertifier = AppPropertiesService.getProperty( "accountgenerator.generation.certifier.family-name", "FC" );
    private static final String birthplaceCodeCertifier = AppPropertiesService.getProperty( "accountgenerator.generation.certifier.birthplace_code", "FC" );
    private static final String birthCountryCodeCertifier = AppPropertiesService.getProperty( "accountgenerator.generation.certifier.birthcountry_code", "FC" );
    private static final String birthCountryCodeValue = AppPropertiesService.getProperty( "accountgenerator.generation.value.birthcountry_code", "FC" );
    private static final String genderCertifier = AppPropertiesService.getProperty( "accountgenerator.generation.certifier.gender", "FC" );
    private static final String birthdateCertifier = AppPropertiesService.getProperty( "accountgenerator.generation.certifier.birthdate", "FC" );
    private static final int birthdateMaxGenerationDay = AppPropertiesService.getPropertyInt( "accountgenerator.generation.value.max.birthdate.day", 15 );
    private static final int birthdateMaxGenerationMonth = AppPropertiesService.getPropertyInt( "accountgenerator.generation.value.max.birthdate.month", 12 );
    private static final int birthdateMaxGenerationYear = AppPropertiesService.getPropertyInt( "accountgenerator.generation.value.max.birthdate.year", 2000 );

    // Generation helpers
    private static final List<String> genders = List.of( "0", "1", "2" );
    private static final List<String> alphabet = List.of( "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
            "U", "V", "W", "X", "Y", "Z" );
    private static final Random random = new Random( );
    private final LocalDate maxGenerationDate;
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern( "dd/MM/yyyy" );

    // Class parameters
    private final RequestClient client;
    private final RequestAuthor author;

    // Constructor is private so that only singleton pattern can use it
    private IdentityAccountGeneratorService( )
    {
        client = new RequestClient( );
        client.setClientId( accountManagementClientId );
        client.setClientSecret( accountManagementSecretId );

        author = new RequestAuthor( );
        author.setName( identityStoreClientName );
        author.setType( AuthorType.application );

        maxGenerationDate = LocalDate.of( birthdateMaxGenerationYear, birthdateMaxGenerationMonth, birthdateMaxGenerationDay );
    }

    /**
     * Create a batch of identities and accounts (optional)
     * 
     * @param accountGenerationDto
     *            the parametrization DTO of the generation
     * @return a list of generated identities and/or accounts
     */
    public List<GeneratedAccountDto> createIdentityAccountBatch( final AccountGenerationDto accountGenerationDto )
    {
        final Date generationDate = new Date( );
        final List<GeneratedAccountDto> generatedAccount = new ArrayList<>( );

        if ( accountGenerationDto != null )
        {
            for ( int i = 1; i <= accountGenerationDto.getBatchSize( ); i++ )
            {
                final GeneratedAccountDto account = new GeneratedAccountDto( );
                generatedAccount.add( account );

                final String mail = "user.".concat( accountGenerationDto.getGenerationPattern( ) ).concat( "." )
                        .concat( String.valueOf( accountGenerationDto.getGenerationIncrementOffset( ) + i ) ).concat( commonMailSuffix );
                account.setPassword( commonPassword );
                account.setEmail( mail );

                // Hold errors in order to clean data if both identity and account cannot be generated
                boolean accountError = false;
                boolean identityError = false;

                // If requested, create a Mon Paris account
                if ( accountGenerationDto.isGenerateAccount( ) )
                {
                    // Create the account
                    try
                    {
                        final ChangeAccountResponse accountCreationResponse = _accountManagementService.createAccount( mail, commonPassword, client );
                        if ( accountCreationResponse.getResult( ) != null && Objects.equals( accountCreationResponse.getStatus( ), "OK" ) )
                        {
                            account.setGuid( accountCreationResponse.getResult( ).getUid( ) );
                            // Validate the account
                            final GetAccountResponse getAccountResponse = _accountManagementService.getAccount( account.getGuid( ), client );
                            final AccountDto accountDto = getAccountResponse.getResult( );
                            accountDto.setValidated( "true" );
                            _accountManagementService.modifyAccount( accountDto, client );
                        }
                        else
                        {
                            account.getStatus( ).add( "The API refused to create the account:\n" + accountCreationResponse );
                            accountError = true;
                        }
                    }
                    catch( final IdentityAccountException e )
                    {
                        account.getStatus( ).add( "An exception occurred when trying to create an account: " + e.getMessage( ) );
                        accountError = true;
                    }
                }
                else
                {
                    account.getStatus( ).add( "No account was requested in this creation request" );
                }

                // Create an identity
                if ( !accountError )
                {
                    final IdentityChangeRequest identityChange = this.buildIdentityChangeRequest( accountGenerationDto, account.getGuid( ), mail, i,
                            generationDate );
                    try
                    {
                        final IdentityChangeResponse createIdentityResponse = _identityService.createIdentity( identityChange, identityStoreClientCode,
                                author );
                        AppLogService.info( "Identity creation request:\n" + identityChange );
                        if ( createIdentityResponse != null
                                && createIdentityResponse.getStatus( ).getHttpCode( ) == ResponseStatusFactory.success( ).getHttpCode( ) )
                        {
                            account.setCuid( createIdentityResponse.getCustomerId( ) );
                        }
                        else
                        {
                            account.getStatus( ).add( "The API refused to create the identity:\n" + createIdentityResponse );
                            identityError = true;
                        }
                    }
                    catch( final IdentityStoreException e )
                    {
                        account.getStatus( ).add( "An exception occurred when trying to create an identity: " + e.getMessage( ) );
                        identityError = true;
                    }

                    // Delete account in case of identity creation fail
                    if ( identityError && accountGenerationDto.isGenerateAccount( ) )
                    {
                        try
                        {
                            final ChangeAccountResponse deleteAccount = _accountManagementService.deleteAccount( account.getGuid( ), client );
                            account.getStatus( ).add( "Tried to delete the account (due to identity creation error): " + deleteAccount );
                        }
                        catch( final IdentityAccountException e )
                        {
                            account.getStatus( )
                                    .add( "An exception occurred when trying to delete the account (due to identity creation error): " + e.getMessage( ) );
                        }
                    }
                }
                else
                {
                    account.getStatus( ).add( "No identity creation due to account creation error" );
                }
            }
        }

        // Store generated account for further treatments
        final List<IdentityAccount> accounts = generatedAccount.stream( ).filter( GeneratedAccountDto::isStorable ).map( a -> {
            final IdentityAccount account = new IdentityAccount( );
            account.setGuid( a.getGuid( ) );
            account.setCuid( a.getCuid( ) );
            account.setCreationDate( generationDate );
            account.setExpirationDate( this.addDays( generationDate, accountGenerationDto.getNbDaysOfValidity( ) ) );
            return account;
        } ).collect( Collectors.toList( ) );

        IdentityAccountHome.saveAccounts( accounts );

        return generatedAccount;
    }

    public IdentityChangeRequest buildIdentityChangeRequest( final AccountGenerationDto accountGenerationDto, final String guid, final String mail,
            int iteration, final Date generationDate )
    {
        final IdentityChangeRequest identityChangeRequest = new IdentityChangeRequest( );
        final IdentityDto identityDto = new IdentityDto( );
        identityDto.setMonParisActive( accountGenerationDto.isGenerateAccount( ) );
        identityDto.setConnectionId( guid );

        final Timestamp now = new Timestamp( System.currentTimeMillis( ) );
        identityDto.setLastUpdateDate( now );
        identityDto.setCreationDate( now );

        final ExpirationDefinition expirationDefinition = new ExpirationDefinition( );
        expirationDefinition.setExpirationDate( Timestamp.valueOf( now.toLocalDateTime( ).plusDays( accountGenerationDto.getNbDaysOfValidity( ) ) ) );
        identityDto.setExpiration( expirationDefinition );

        final LocalDate birthdate = this.getRandomDate( );

        identityDto.getAttributes( ).add( this.buildAttribute( Constants.PARAM_GENDER, this.getRandomGender( ), genderCertifier, generationDate ) );
        identityDto.getAttributes( ).add( this.buildAttribute( Constants.PARAM_FIRST_NAME,
                this.getRandomAttribute( Constants.PARAM_FIRST_NAME, accountGenerationDto, iteration, " " ), firstNameCertifier, generationDate ) );
        identityDto.getAttributes( ).add( this.buildAttribute( Constants.PARAM_FAMILY_NAME,
                this.getRandomAttribute( Constants.PARAM_FAMILY_NAME, accountGenerationDto, iteration, " " ), familyNameCertifier, generationDate ) );
        identityDto.getAttributes( )
                .add( this.buildAttribute( Constants.PARAM_BIRTH_DATE, this.getLocaleDateAsString( birthdate ), birthdateCertifier, generationDate ) );
        identityDto.getAttributes( )
                .add( this.buildAttribute( Constants.PARAM_BIRTH_COUNTRY_CODE, birthCountryCodeValue, birthCountryCodeCertifier, generationDate ) );
        identityDto.getAttributes( ).add(
                this.buildAttribute( Constants.PARAM_BIRTH_PLACE_CODE, this.getRandomBirthplaceCode( birthdate ), birthplaceCodeCertifier, generationDate ) );
        identityDto.getAttributes( ).add( this.buildAttribute( Constants.PARAM_EMAIL, mail, mailCertifier, generationDate ) );
        identityDto.getAttributes( ).add( this.buildAttribute( Constants.PARAM_LOGIN,
                this.getRandomAttribute( Constants.PARAM_LOGIN, accountGenerationDto, iteration, "-" ), loginCertifier, generationDate ) );

        identityChangeRequest.setIdentity( identityDto );

        return identityChangeRequest;
    }

    private String getRandomBirthplaceCode( final LocalDate birthdate )
    {
        final List<String> codesFromCache = _geocodesCache.getCodesFromCache( java.sql.Date.valueOf( birthdate ) );
        return codesFromCache.get( random.nextInt( codesFromCache.size( ) ) );
    }

    private String getRandomAttribute( final String attributeName, final AccountGenerationDto accountGenerationDto, final int iteration,
            final String separator )
    {
        return attributeName.replace( "_", "" ).concat( separator ).concat( accountGenerationDto.getGenerationPattern( ) ).concat( separator )
                .concat( this.toLetters( accountGenerationDto.getGenerationIncrementOffset( ) + iteration ) );
    }

    private String toLetters( int iteration )
    {
        final String strValue = String.valueOf( iteration );
        String strReturn = "";
        for ( int i = 0; i < strValue.length( ); i++ )
        {
            strReturn = strReturn.concat( alphabet.get( i ) );
        }
        return strReturn;
    }

    private String getRandomGender( )
    {
        return genders.get( random.nextInt( genders.size( ) ) );
    }

    public LocalDate getRandomDate( )
    {
        final long randomDay = ThreadLocalRandom.current( ).nextLong( maxGenerationDate.toEpochDay( ) );
        return LocalDate.ofEpochDay( randomDay );
    }

    public String getLocaleDateAsString( final LocalDate date )
    {
        return date.format( dateTimeFormatter );
    }

    public AttributeDto buildAttribute( final String key, final String value, final String certifier, final Date certificationDate )
    {
        final AttributeDto attribute = new AttributeDto( );

        attribute.setValue( value );
        attribute.setKey( key );
        attribute.setCertifier( certifier );
        attribute.setCertificationDate( certificationDate );

        return attribute;
    }

    private Date add( final Date date, final int calendarField, final int amount )
    {
        final Calendar c = Calendar.getInstance( );
        c.setTime( date );
        c.add( calendarField, amount );
        return c.getTime( );
    }

    public Date addDays( final Date date, final int amount )
    {
        return this.add( date, Calendar.DAY_OF_MONTH, amount );
    }
}
