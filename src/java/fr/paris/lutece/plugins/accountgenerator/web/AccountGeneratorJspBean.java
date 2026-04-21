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
package fr.paris.lutece.plugins.accountgenerator.web;

import fr.paris.lutece.plugins.accountgenerator.service.IdentityAccountGeneratorService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.account.generator.AccountGenerationDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.account.generator.GeneratedAccountDto;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.security.SecurityTokenService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * This class provides the user interface to generate accounts and identities from the back office.
 */
@Controller( controllerJsp = "AccountGeneratorManagement.jsp", controllerPath = "jsp/admin/plugins/accountgenerator/", right = "ACCOUNTGENERATOR_MANAGEMENT" )
public class AccountGeneratorJspBean extends MVCAdminJspBean
{
    private static final long serialVersionUID = 1L;

    // Templates
    private static final String TEMPLATE_GENERATE_ACCOUNTS = "/admin/plugins/accountgenerator/generate_accounts.html";

    // Parameters
    private static final String PARAMETER_GENERATE_ACCOUNT = "generate_account";
    private static final String PARAMETER_GENERATION_INCREMENT_OFFSET = "generation_increment_offset";
    private static final String PARAMETER_NB_DAYS_OF_VALIDITY = "nb_days_of_validity";
    private static final String PARAMETER_BATCH_SIZE = "batch_size";
    private static final String PARAMETER_LOGIN_PREFIX = "login_prefix";
    private static final String PARAMETER_LOGIN_SUFFIX = "login_suffix";
    private static final String PARAMETER_PASSWORD = "password";
    private static final String PARAMETER_FIRST_NAME_PREFIX = "first_name_prefix";
    private static final String PARAMETER_FAMILY_NAME_PREFIX = "family_name_prefix";
    private static final String PARAMETER_BIRTHDATE = "birthdate";
    private static final String PARAMETER_BIRTH_COUNTRY_CODE = "birth_country_code";
    private static final String PARAMETER_BIRTHPLACE_CODE = "birthplace_code";
    private static final String PARAMETER_IDENTITY_CERTIFIER = "identity_certifier";
    private static final String PARAMETER_MAIL_LOGIN_CERTIFIER = "mail_login_certifier";

    // Properties
    private static final String PROPERTY_PAGE_TITLE = "accountgenerator.generate_accounts.pageTitle";
    private static final int GENERATION_LIMIT = AppPropertiesService.getPropertyInt( "accountgenerator.generation.limit", 50 );

    // Markers
    private static final String MARK_GENERATED_ACCOUNTS = "generated_accounts";
    private static final String MARK_GENERATION_MESSAGE = "generation_message";
    private static final String MARK_GENERATION_LIMIT = "generation_limit";

    // Views
    private static final String VIEW_GENERATE_ACCOUNTS = "generateAccounts";

    // Actions
    private static final String ACTION_GENERATE_ACCOUNTS = "doGenerateAccounts";

    // Infos
    private static final String INFO_GENERATION_SUCCESS = "accountgenerator.info.generation.success";
    private static final String INFO_GENERATION_RESULT = "accountgenerator.info.generation.result";

    // Errors
    private static final String ERROR_GENERATION_FAILED = "accountgenerator.error.generation.failed";
    private static final String ERROR_INVALID_PARAMETERS = "accountgenerator.error.generation.invalidParameters";

    // Markers for form field values
    private static final String MARK_LOGIN_PREFIX = "login_prefix";
    private static final String MARK_LOGIN_SUFFIX = "login_suffix";
    private static final String MARK_GENERATION_INCREMENT_OFFSET = "generation_increment_offset";
    private static final String MARK_BATCH_SIZE = "batch_size";
    private static final String MARK_NB_DAYS_OF_VALIDITY = "nb_days_of_validity";
    private static final String MARK_PASSWORD = "password";
    private static final String MARK_FIRST_NAME_PREFIX = "first_name_prefix";
    private static final String MARK_FAMILY_NAME_PREFIX = "family_name_prefix";
    private static final String MARK_BIRTHDATE = "birthdate";
    private static final String MARK_BIRTH_COUNTRY_CODE = "birth_country_code";
    private static final String MARK_BIRTHPLACE_CODE = "birthplace_code";
    private static final String MARK_IDENTITY_CERTIFIER = "identity_certifier";
    private static final String MARK_MAIL_LOGIN_CERTIFIER = "mail_login_certifier";
    private static final String MARK_GENERATE_ACCOUNT = "generate_account";

    // Default values
    private static final String DEFAULT_LOGIN_PREFIX = "testperfpf";
    private static final String DEFAULT_LOGIN_SUFFIX = "@yopmail.com";
    private static final String DEFAULT_GENERATION_INCREMENT_OFFSET = "0";
    private static final String DEFAULT_BATCH_SIZE = "5";
    private static final String DEFAULT_NB_DAYS_OF_VALIDITY = "300";
    private static final String DEFAULT_PASSWORD = "Changeme1!";
    private static final String DEFAULT_FIRST_NAME_PREFIX = "Prenom";
    private static final String DEFAULT_FAMILY_NAME_PREFIX = "NOM";
    private static final String DEFAULT_BIRTHDATE = "01/01/2000";
    private static final String DEFAULT_BIRTH_COUNTRY_CODE = "99100";
    private static final String DEFAULT_BIRTHPLACE_CODE = "75112";
    private static final String DEFAULT_IDENTITY_CERTIFIER = "fccertifier";
    private static final String DEFAULT_MAIL_LOGIN_CERTIFIER = "DEC";

    // Session state
    private transient List<GeneratedAccountDto> _generatedAccounts;
    private String _strGenerationMessage;
    private Map<String, String> _lastFormValues;

    /**
     * Build the generate accounts form view
     *
     * @param request
     *            The HTTP request
     * @return The page
     */
    @View( value = VIEW_GENERATE_ACCOUNTS, defaultView = true )
    public String getGenerateAccounts( HttpServletRequest request )
    {
        Map<String, Object> model = getModel( );
        model.put( MARK_GENERATION_LIMIT, GENERATION_LIMIT );

        // Populate form fields with last submitted values or defaults
        model.put( MARK_LOGIN_PREFIX, getFormValue( MARK_LOGIN_PREFIX, DEFAULT_LOGIN_PREFIX ) );
        model.put( MARK_LOGIN_SUFFIX, getFormValue( MARK_LOGIN_SUFFIX, DEFAULT_LOGIN_SUFFIX ) );
        model.put( MARK_GENERATION_INCREMENT_OFFSET, getFormValue( MARK_GENERATION_INCREMENT_OFFSET, DEFAULT_GENERATION_INCREMENT_OFFSET ) );
        model.put( MARK_BATCH_SIZE, getFormValue( MARK_BATCH_SIZE, DEFAULT_BATCH_SIZE ) );
        model.put( MARK_NB_DAYS_OF_VALIDITY, getFormValue( MARK_NB_DAYS_OF_VALIDITY, DEFAULT_NB_DAYS_OF_VALIDITY ) );
        model.put( MARK_PASSWORD, getFormValue( MARK_PASSWORD, DEFAULT_PASSWORD ) );
        model.put( MARK_FIRST_NAME_PREFIX, getFormValue( MARK_FIRST_NAME_PREFIX, DEFAULT_FIRST_NAME_PREFIX ) );
        model.put( MARK_FAMILY_NAME_PREFIX, getFormValue( MARK_FAMILY_NAME_PREFIX, DEFAULT_FAMILY_NAME_PREFIX ) );
        model.put( MARK_BIRTHDATE, getFormValue( MARK_BIRTHDATE, DEFAULT_BIRTHDATE ) );
        model.put( MARK_BIRTH_COUNTRY_CODE, getFormValue( MARK_BIRTH_COUNTRY_CODE, DEFAULT_BIRTH_COUNTRY_CODE ) );
        model.put( MARK_BIRTHPLACE_CODE, getFormValue( MARK_BIRTHPLACE_CODE, DEFAULT_BIRTHPLACE_CODE ) );
        model.put( MARK_IDENTITY_CERTIFIER, getFormValue( MARK_IDENTITY_CERTIFIER, DEFAULT_IDENTITY_CERTIFIER ) );
        model.put( MARK_MAIL_LOGIN_CERTIFIER, getFormValue( MARK_MAIL_LOGIN_CERTIFIER, DEFAULT_MAIL_LOGIN_CERTIFIER ) );
        model.put( MARK_GENERATE_ACCOUNT, _lastFormValues != null && _lastFormValues.containsKey( MARK_GENERATE_ACCOUNT ) );

        if ( _generatedAccounts != null )
        {
            model.put( MARK_GENERATED_ACCOUNTS, _generatedAccounts );
            model.put( MARK_GENERATION_MESSAGE, _strGenerationMessage );
            _generatedAccounts = null;
            _strGenerationMessage = null;
        }

        model.put( SecurityTokenService.MARK_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_GENERATE_ACCOUNTS ) );

        return getPage( PROPERTY_PAGE_TITLE, TEMPLATE_GENERATE_ACCOUNTS, model );
    }

    private String getFormValue( String key, String defaultValue )
    {
        if ( _lastFormValues != null && _lastFormValues.containsKey( key ) )
        {
            return _lastFormValues.get( key );
        }
        return defaultValue;
    }

    /**
     * Process the account generation action
     *
     * @param request
     *            The HTTP request
     * @return The redirect URL
     * @throws AccessDeniedException
     *             if the security token is invalid
     */
    @Action( ACTION_GENERATE_ACCOUNTS )
    public String doGenerateAccounts( HttpServletRequest request ) throws AccessDeniedException
    {
        if ( !SecurityTokenService.getInstance( ).validate( request, ACTION_GENERATE_ACCOUNTS ) )
        {
            throw new AccessDeniedException( "Invalid security token" );
        }

        final String strGenerateAccount = request.getParameter( PARAMETER_GENERATE_ACCOUNT );
        final String strOffset = request.getParameter( PARAMETER_GENERATION_INCREMENT_OFFSET );
        final String strNbDays = request.getParameter( PARAMETER_NB_DAYS_OF_VALIDITY );
        final String strBatchSize = request.getParameter( PARAMETER_BATCH_SIZE );
        final String strLoginPrefix = request.getParameter( PARAMETER_LOGIN_PREFIX );
        final String strLoginSuffix = request.getParameter( PARAMETER_LOGIN_SUFFIX );
        final String strPassword = request.getParameter( PARAMETER_PASSWORD );
        final String strFirstNamePrefix = request.getParameter( PARAMETER_FIRST_NAME_PREFIX );
        final String strFamilyNamePrefix = request.getParameter( PARAMETER_FAMILY_NAME_PREFIX );
        final String strBirthdate = request.getParameter( PARAMETER_BIRTHDATE );
        final String strBirthCountryCode = request.getParameter( PARAMETER_BIRTH_COUNTRY_CODE );
        final String strBirthplaceCode = request.getParameter( PARAMETER_BIRTHPLACE_CODE );
        final String strIdentityCertifier = request.getParameter( PARAMETER_IDENTITY_CERTIFIER );
        final String strMailLoginCertifier = request.getParameter( PARAMETER_MAIL_LOGIN_CERTIFIER );

        // Save form values for re-display after redirect
        _lastFormValues = new java.util.HashMap<>( );
        _lastFormValues.put( MARK_LOGIN_PREFIX, strLoginPrefix );
        _lastFormValues.put( MARK_LOGIN_SUFFIX, strLoginSuffix );
        _lastFormValues.put( MARK_GENERATION_INCREMENT_OFFSET, strOffset );
        _lastFormValues.put( MARK_BATCH_SIZE, strBatchSize );
        _lastFormValues.put( MARK_NB_DAYS_OF_VALIDITY, strNbDays );
        _lastFormValues.put( MARK_PASSWORD, strPassword );
        _lastFormValues.put( MARK_FIRST_NAME_PREFIX, strFirstNamePrefix );
        _lastFormValues.put( MARK_FAMILY_NAME_PREFIX, strFamilyNamePrefix );
        _lastFormValues.put( MARK_BIRTHDATE, strBirthdate );
        _lastFormValues.put( MARK_BIRTH_COUNTRY_CODE, strBirthCountryCode );
        _lastFormValues.put( MARK_BIRTHPLACE_CODE, strBirthplaceCode );
        _lastFormValues.put( MARK_IDENTITY_CERTIFIER, strIdentityCertifier );
        _lastFormValues.put( MARK_MAIL_LOGIN_CERTIFIER, strMailLoginCertifier );
        if ( strGenerateAccount != null )
        {
            _lastFormValues.put( MARK_GENERATE_ACCOUNT, strGenerateAccount );
        }

        if ( strOffset == null || strNbDays == null || strBatchSize == null
                || strLoginPrefix == null || strLoginPrefix.trim( ).isEmpty( )
                || strLoginSuffix == null || strLoginSuffix.trim( ).isEmpty( ) )
        {
            addError( ERROR_INVALID_PARAMETERS, getLocale( ) );
            return redirectView( request, VIEW_GENERATE_ACCOUNTS );
        }

        try
        {
            final AccountGenerationDto accountGenerationDto = new AccountGenerationDto( );
            accountGenerationDto.setGenerateAccount( "on".equals( strGenerateAccount ) || "true".equals( strGenerateAccount ) );
            accountGenerationDto.setGenerationIncrementOffset( Integer.parseInt( strOffset ) );
            accountGenerationDto.setNbDaysOfValidity( Integer.parseInt( strNbDays ) );
            accountGenerationDto.setLoginPrefix( strLoginPrefix.trim( ) );
            accountGenerationDto.setLoginSuffix( strLoginSuffix.trim( ) );

            if ( strPassword != null && !strPassword.trim( ).isEmpty( ) )
            {
                accountGenerationDto.setPassword( strPassword.trim( ) );
            }
            if ( strFirstNamePrefix != null && !strFirstNamePrefix.trim( ).isEmpty( ) )
            {
                accountGenerationDto.setFirstNamePrefix( strFirstNamePrefix.trim( ) );
            }
            if ( strFamilyNamePrefix != null && !strFamilyNamePrefix.trim( ).isEmpty( ) )
            {
                accountGenerationDto.setFamilyNamePrefix( strFamilyNamePrefix.trim( ) );
            }
            if ( strBirthdate != null && !strBirthdate.trim( ).isEmpty( ) )
            {
                accountGenerationDto.setBirthdate( strBirthdate.trim( ) );
            }
            if ( strBirthCountryCode != null && !strBirthCountryCode.trim( ).isEmpty( ) )
            {
                accountGenerationDto.setBirthCountryCode( strBirthCountryCode.trim( ) );
            }
            if ( strBirthplaceCode != null && !strBirthplaceCode.trim( ).isEmpty( ) )
            {
                accountGenerationDto.setBirthplaceCode( strBirthplaceCode.trim( ) );
            }
            if ( strIdentityCertifier != null && !strIdentityCertifier.trim( ).isEmpty( ) )
            {
                accountGenerationDto.setIdentityCertifier( strIdentityCertifier.trim( ) );
            }
            if ( strMailLoginCertifier != null && !strMailLoginCertifier.trim( ).isEmpty( ) )
            {
                accountGenerationDto.setMailLoginCertifier( strMailLoginCertifier.trim( ) );
            }

            final int batchSize = Math.min( Integer.parseInt( strBatchSize ), GENERATION_LIMIT );
            accountGenerationDto.setBatchSize( batchSize );

            _generatedAccounts = IdentityAccountGeneratorService.instance( ).createIdentityAccountBatch( accountGenerationDto );

            final long accountsCount = _generatedAccounts.stream( ).filter( GeneratedAccountDto::hasAccount ).count( );
            final long identitiesCount = _generatedAccounts.stream( ).filter( GeneratedAccountDto::hasIdentity ).count( );
            _strGenerationMessage = I18nService.getLocalizedString( INFO_GENERATION_RESULT, new String [ ] {
                    String.valueOf( accountsCount ), String.valueOf( identitiesCount )
            }, getLocale( ) );

            addInfo( INFO_GENERATION_SUCCESS, getLocale( ) );
        }
        catch( final NumberFormatException e )
        {
            addError( ERROR_INVALID_PARAMETERS, getLocale( ) );
        }
        catch( final Exception e )
        {
            AppLogService.error( "Error generating accounts", e );
            addError( ERROR_GENERATION_FAILED, getLocale( ) );
        }

        return redirectView( request, VIEW_GENERATE_ACCOUNTS );
    }
}
