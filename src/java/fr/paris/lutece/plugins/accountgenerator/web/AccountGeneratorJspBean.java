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

import fr.paris.lutece.plugins.accountgenerator.business.AccountGenerationJob;
import fr.paris.lutece.plugins.accountgenerator.service.AccountGenerationJobService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.account.generator.AccountGenerationDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AuthorType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.RequestAuthor;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.referentiel.AttributeCertificationProcessusDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.referentiel.ProcessusSearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.service.ReferentialService;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestFormatException;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.progressmanager.ProgressManagerService;
import fr.paris.lutece.portal.service.security.SecurityTokenService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.util.ReferenceList;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;

@Controller( controllerJsp = "AccountGeneratorManagement.jsp", controllerPath = "jsp/admin/plugins/accountgenerator/", right = "ACCOUNTGENERATOR_MANAGEMENT" )
public class AccountGeneratorJspBean extends MVCAdminJspBean
{
    private static final long serialVersionUID = 1L;

    private static final String TEMPLATE_MANAGE_JOBS = "/admin/plugins/accountgenerator/manage_jobs.html";
    private static final String TEMPLATE_CREATE_JOB = "/admin/plugins/accountgenerator/generate_accounts.html";
    private static final String TEMPLATE_VIEW_JOB = "/admin/plugins/accountgenerator/view_job.html";

    private static final String PROPERTY_PAGE_TITLE_MANAGE = "accountgenerator.manage_jobs.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_CREATE = "accountgenerator.generate_accounts.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_VIEW = "accountgenerator.view_job.pageTitle";

    private static final int GENERATION_LIMIT = AppPropertiesService.getPropertyInt( "accountgenerator.generation.limit", 100000 );

    private static final String VIEW_MANAGE_JOBS = "manageJobs";
    private static final String VIEW_CREATE_JOB = "createJob";
    private static final String VIEW_JOB_DETAIL = "viewJob";

    private static final String ACTION_SUBMIT_JOB = "submitJob";

    // Form field names (used both as request parameters and as model keys — the template references `${login_prefix}` etc.)
    private static final String FIELD_GENERATE_ACCOUNT = "generate_account";
    private static final String FIELD_GENERATION_INCREMENT_OFFSET = "generation_increment_offset";
    private static final String FIELD_NB_DAYS_OF_VALIDITY = "nb_days_of_validity";
    private static final String FIELD_BATCH_SIZE = "batch_size";
    private static final String FIELD_LOGIN_PREFIX = "login_prefix";
    private static final String FIELD_LOGIN_SUFFIX = "login_suffix";
    private static final String FIELD_PASSWORD = "password";
    private static final String FIELD_FIRST_NAME_PREFIX = "first_name_prefix";
    private static final String FIELD_FAMILY_NAME_PREFIX = "family_name_prefix";
    private static final String FIELD_BIRTHDATE = "birthdate";
    private static final String FIELD_BIRTH_COUNTRY_CODE = "birth_country_code";
    private static final String FIELD_BIRTHPLACE_CODE = "birthplace_code";
    private static final String FIELD_IDENTITY_CERTIFIER = "identity_certifier";
    private static final String FIELD_MAIL_LOGIN_CERTIFIER = "mail_login_certifier";

    private static final String PARAMETER_REFERENCE = "reference";

    // Default form values (mirrored from the template)
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

    private static final String MARK_JOBS = "jobs";
    private static final String MARK_JOB = "job";
    private static final String MARK_GENERATION_LIMIT = "generation_limit";
    private static final String MARK_FEED_TOKEN = "feed_token";
    private static final String MARK_DELETION_FEED_TOKEN = "deletion_feed_token";
    private static final String MARK_PROGRESS_PERCENT = "progress_percent";
    private static final String MARK_DOWNLOAD_URL = "download_url";
    private static final String MARK_PREVIEW_ROWS = "preview_rows";
    private static final String MARK_DELETE_TOKEN = "delete_token";

    private static final String MARK_CERTIFIER_LIST = "certifier_list";

    private static final String ERROR_INVALID_PARAMETERS = "accountgenerator.error.generation.invalidParameters";
    private static final String ERROR_JOB_NOT_FOUND = "accountgenerator.error.job.notFound";
    private static final String ERROR_FIELD_LOGIN_PREFIX = "accountgenerator.error.field.loginPrefix";
    private static final String ERROR_FIELD_LOGIN_SUFFIX = "accountgenerator.error.field.loginSuffix";
    private static final String ERROR_FIELD_BATCH_SIZE = "accountgenerator.error.field.batchSize";
    private static final String ERROR_FIELD_NB_DAYS = "accountgenerator.error.field.nbDaysOfValidity";
    private static final String ERROR_FIELD_OFFSET = "accountgenerator.error.field.generationIncrementOffset";
    private static final String ERROR_FIELD_BIRTHDATE = "accountgenerator.error.field.birthdate";
    private static final String INFO_JOB_SUBMITTED = "accountgenerator.info.job.submitted";
    private static final String INFO_DELETION_SUBMITTED = "accountgenerator.info.job.deletionSubmitted";

    private static final String ACTION_DELETE_JOB_ACCOUNTS = "deleteJobAccounts";

    // Remembered form values for re-display on validation error
    private transient Map<String, String> _lastFormValues;

    @View( value = VIEW_MANAGE_JOBS, defaultView = true )
    public String getManageJobs( final HttpServletRequest request )
    {
        final List<AccountGenerationJob> jobs = AccountGenerationJobService.instance( ).findAll( );
        final Map<String, Object> model = getModel( );
        model.put( MARK_JOBS, jobs );
        return getPage( PROPERTY_PAGE_TITLE_MANAGE, TEMPLATE_MANAGE_JOBS, model );
    }

    @View( VIEW_CREATE_JOB )
    public String getCreateJob( final HttpServletRequest request )
    {
        final Map<String, Object> model = getModel( );
        model.put( MARK_GENERATION_LIMIT, GENERATION_LIMIT );

        model.put( FIELD_LOGIN_PREFIX, getFormValue( FIELD_LOGIN_PREFIX, DEFAULT_LOGIN_PREFIX ) );
        model.put( FIELD_LOGIN_SUFFIX, getFormValue( FIELD_LOGIN_SUFFIX, DEFAULT_LOGIN_SUFFIX ) );
        model.put( FIELD_GENERATION_INCREMENT_OFFSET, getFormValue( FIELD_GENERATION_INCREMENT_OFFSET, DEFAULT_GENERATION_INCREMENT_OFFSET ) );
        model.put( FIELD_BATCH_SIZE, getFormValue( FIELD_BATCH_SIZE, DEFAULT_BATCH_SIZE ) );
        model.put( FIELD_NB_DAYS_OF_VALIDITY, getFormValue( FIELD_NB_DAYS_OF_VALIDITY, DEFAULT_NB_DAYS_OF_VALIDITY ) );
        model.put( FIELD_PASSWORD, getFormValue( FIELD_PASSWORD, DEFAULT_PASSWORD ) );
        model.put( FIELD_FIRST_NAME_PREFIX, getFormValue( FIELD_FIRST_NAME_PREFIX, DEFAULT_FIRST_NAME_PREFIX ) );
        model.put( FIELD_FAMILY_NAME_PREFIX, getFormValue( FIELD_FAMILY_NAME_PREFIX, DEFAULT_FAMILY_NAME_PREFIX ) );
        model.put( FIELD_BIRTHDATE, getFormValue( FIELD_BIRTHDATE, DEFAULT_BIRTHDATE ) );
        model.put( FIELD_BIRTH_COUNTRY_CODE, getFormValue( FIELD_BIRTH_COUNTRY_CODE, DEFAULT_BIRTH_COUNTRY_CODE ) );
        model.put( FIELD_BIRTHPLACE_CODE, getFormValue( FIELD_BIRTHPLACE_CODE, DEFAULT_BIRTHPLACE_CODE ) );
        model.put( FIELD_IDENTITY_CERTIFIER, getFormValue( FIELD_IDENTITY_CERTIFIER, DEFAULT_IDENTITY_CERTIFIER ) );
        model.put( FIELD_MAIL_LOGIN_CERTIFIER, getFormValue( FIELD_MAIL_LOGIN_CERTIFIER, DEFAULT_MAIL_LOGIN_CERTIFIER ) );
        model.put( FIELD_GENERATE_ACCOUNT, _lastFormValues == null || _lastFormValues.containsKey( FIELD_GENERATE_ACCOUNT ) );
        model.put( MARK_CERTIFIER_LIST, loadCertifierList( ) );

        model.put( SecurityTokenService.MARK_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_SUBMIT_JOB ) );
        return getPage( PROPERTY_PAGE_TITLE_CREATE, TEMPLATE_CREATE_JOB, model );
    }

    private ReferenceList loadCertifierList( )
    {
        final ReferenceList list = new ReferenceList( );
        try
        {
            final ReferentialService referentialService = SpringContextService.getBean( "accountgenerator.referentialService" );
            final RequestAuthor author = new RequestAuthor( );
            author.setType( AuthorType.application );
            author.setName( AppPropertiesService.getProperty( "accountgenerator.accountManagement.client-name", "AccountGenerator" ) );

            final String clientCode = AppPropertiesService.getProperty( "accountgenerator.accountManagement.client-code", "TEST" );
            final ProcessusSearchResponse response = referentialService.getProcessList( clientCode, author );
            if ( response != null && response.getProcessus( ) != null )
            {
                for ( final AttributeCertificationProcessusDto processus : response.getProcessus( ) )
                {
                    list.addItem( processus.getCode( ), processus.getLabel( ) );
                }
            }
        }
        catch( final Exception e )
        {
            AppLogService.error( "Failed to load certification processus list", e );
        }
        return list;
    }

    @View( VIEW_JOB_DETAIL )
    public String getViewJob( final HttpServletRequest request )
    {
        final String reference = request.getParameter( PARAMETER_REFERENCE );
        if ( reference == null )
        {
            return redirectView( request, VIEW_MANAGE_JOBS );
        }
        final Optional<AccountGenerationJob> optJob = AccountGenerationJobService.instance( ).findByReference( reference );
        if ( !optJob.isPresent( ) )
        {
            addError( ERROR_JOB_NOT_FOUND, getLocale( ) );
            return redirectView( request, VIEW_MANAGE_JOBS );
        }

        final AccountGenerationJob job = optJob.get( );
        final Map<String, Object> model = getModel( );
        model.put( MARK_JOB, job );

        final String feedToken = AccountGenerationJobService.instance( ).getProgressFeedToken( job.getReference( ) );
        if ( feedToken != null )
        {
            model.put( MARK_FEED_TOKEN, feedToken );
            model.put( MARK_PROGRESS_PERCENT, ProgressManagerService.getInstance( ).getProgressStatus( feedToken ) );
        }
        else if ( job.getBatchSize( ) > 0 )
        {
            model.put( MARK_PROGRESS_PERCENT, (int) ( job.getNbProcessed( ) * 100.0 / job.getBatchSize( ) + 0.5 ) );
        }

        final String deletionFeedToken = AccountGenerationJobService.instance( ).getDeletionProgressFeedToken( job.getReference( ) );
        if ( deletionFeedToken != null )
        {
            model.put( MARK_DELETION_FEED_TOKEN, deletionFeedToken );
        }

        if ( AccountGenerationJobService.instance( ).hasDownloadableFile( job ) )
        {
            model.put( MARK_DOWNLOAD_URL, "jsp/admin/plugins/accountgenerator/DownloadCsv.jsp?reference=" + job.getReference( ) );
        }

        model.put( MARK_PREVIEW_ROWS, AccountGenerationJobService.instance( ).getCsvFirstAndLast( job ) );
        model.put( MARK_DELETE_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_DELETE_JOB_ACCOUNTS ) );

        return getPage( PROPERTY_PAGE_TITLE_VIEW, TEMPLATE_VIEW_JOB, model );
    }

    @Action( ACTION_DELETE_JOB_ACCOUNTS )
    public String doDeleteJobAccounts( final HttpServletRequest request ) throws AccessDeniedException
    {
        if ( !SecurityTokenService.getInstance( ).validate( request, ACTION_DELETE_JOB_ACCOUNTS ) )
        {
            throw new AccessDeniedException( "Invalid security token" );
        }
        final String reference = request.getParameter( PARAMETER_REFERENCE );
        if ( reference == null )
        {
            return redirectView( request, VIEW_MANAGE_JOBS );
        }
        try
        {
            final AccountGenerationJob job = AccountGenerationJobService.instance( ).deleteGeneratedAccounts( reference );
            if ( job == null )
            {
                addError( ERROR_JOB_NOT_FOUND, getLocale( ) );
                return redirectView( request, VIEW_MANAGE_JOBS );
            }
            addInfo( INFO_DELETION_SUBMITTED, getLocale( ) );
        }
        catch( final Exception e )
        {
            AppLogService.error( "Failed to delete accounts for job " + reference, e );
            addError( e.getMessage( ) );
        }
        return redirect( request, VIEW_JOB_DETAIL, Collections.singletonMap( PARAMETER_REFERENCE, reference ) );
    }

    @Action( ACTION_SUBMIT_JOB )
    public String doSubmitJob( final HttpServletRequest request ) throws AccessDeniedException
    {
        if ( !SecurityTokenService.getInstance( ).validate( request, ACTION_SUBMIT_JOB ) )
        {
            throw new AccessDeniedException( "Invalid security token" );
        }

        rememberFormValues( request );

        final AccountGenerationDto dto;
        try
        {
            dto = buildDto( request );
        }
        catch( final NumberFormatException e )
        {
            addError( ERROR_INVALID_PARAMETERS, getLocale( ) );
            return redirectView( request, VIEW_CREATE_JOB );
        }

        // Field-level validation
        if ( !validateDto( dto ) )
        {
            return redirectView( request, VIEW_CREATE_JOB );
        }

        try
        {
            final String user = getUser( ) != null ? getUser( ).getAccessCode( ) : null;
            final AccountGenerationJob job = AccountGenerationJobService.instance( ).submit( dto, "BO", "accountgenerator-admin", user, "admin", user );
            _lastFormValues = null;
            addInfo( INFO_JOB_SUBMITTED, getLocale( ) );
            return redirect( request, VIEW_JOB_DETAIL, Collections.singletonMap( PARAMETER_REFERENCE, job.getReference( ) ) );
        }
        catch( final RequestFormatException e )
        {
            addError( e.getMessage( ) );
            return redirectView( request, VIEW_CREATE_JOB );
        }
        catch( final Exception e )
        {
            AppLogService.error( "Failed to submit account generation job", e );
            addError( e.getMessage( ) );
            return redirectView( request, VIEW_CREATE_JOB );
        }
    }

    private boolean validateDto( final AccountGenerationDto dto )
    {
        boolean valid = true;

        if ( dto.getLoginPrefix( ) == null || dto.getLoginPrefix( ).isEmpty( ) )
        {
            addError( ERROR_FIELD_LOGIN_PREFIX, getLocale( ) );
            valid = false;
        }
        if ( dto.getLoginSuffix( ) == null || dto.getLoginSuffix( ).isEmpty( ) )
        {
            addError( ERROR_FIELD_LOGIN_SUFFIX, getLocale( ) );
            valid = false;
        }
        if ( dto.getBatchSize( ) <= 0 || dto.getBatchSize( ) > GENERATION_LIMIT )
        {
            addError( fr.paris.lutece.portal.service.i18n.I18nService.getLocalizedString( ERROR_FIELD_BATCH_SIZE,
                    new String [ ] { String.valueOf( GENERATION_LIMIT ) }, getLocale( ) ) );
            valid = false;
        }
        if ( dto.getNbDaysOfValidity( ) <= 0 )
        {
            addError( ERROR_FIELD_NB_DAYS, getLocale( ) );
            valid = false;
        }
        if ( dto.getGenerationIncrementOffset( ) < 0 )
        {
            addError( ERROR_FIELD_OFFSET, getLocale( ) );
            valid = false;
        }
        if ( dto.getBirthdate( ) != null && !dto.getBirthdate( ).isEmpty( ) )
        {
            try
            {
                java.time.LocalDate.parse( dto.getBirthdate( ).trim( ), java.time.format.DateTimeFormatter.ofPattern( "dd/MM/yyyy" ) );
            }
            catch( final java.time.format.DateTimeParseException e )
            {
                addError( ERROR_FIELD_BIRTHDATE, getLocale( ) );
                valid = false;
            }
        }

        return valid;
    }

    private String getFormValue( final String key, final String defaultValue )
    {
        if ( _lastFormValues != null && _lastFormValues.containsKey( key ) )
        {
            return _lastFormValues.get( key );
        }
        return defaultValue;
    }

    private void rememberFormValues( final HttpServletRequest request )
    {
        _lastFormValues = new HashMap<>( );
        for ( final String field : new String [ ] {
                FIELD_LOGIN_PREFIX, FIELD_LOGIN_SUFFIX, FIELD_GENERATION_INCREMENT_OFFSET, FIELD_BATCH_SIZE, FIELD_NB_DAYS_OF_VALIDITY, FIELD_PASSWORD,
                FIELD_FIRST_NAME_PREFIX, FIELD_FAMILY_NAME_PREFIX, FIELD_BIRTHDATE, FIELD_BIRTH_COUNTRY_CODE, FIELD_BIRTHPLACE_CODE, FIELD_IDENTITY_CERTIFIER,
                FIELD_MAIL_LOGIN_CERTIFIER
        } )
        {
            final String value = request.getParameter( field );
            if ( value != null )
            {
                _lastFormValues.put( field, value );
            }
        }
        final String generateAccount = request.getParameter( FIELD_GENERATE_ACCOUNT );
        if ( generateAccount != null )
        {
            _lastFormValues.put( FIELD_GENERATE_ACCOUNT, generateAccount );
        }
    }

    private AccountGenerationDto buildDto( final HttpServletRequest request )
    {
        final AccountGenerationDto dto = new AccountGenerationDto( );
        final String strGenerateAccount = request.getParameter( FIELD_GENERATE_ACCOUNT );
        dto.setGenerateAccount( "on".equals( strGenerateAccount ) || "true".equals( strGenerateAccount ) );
        dto.setGenerationIncrementOffset( parseInt( request.getParameter( FIELD_GENERATION_INCREMENT_OFFSET ) ) );
        dto.setNbDaysOfValidity( parseInt( request.getParameter( FIELD_NB_DAYS_OF_VALIDITY ) ) );
        dto.setBatchSize( parseInt( request.getParameter( FIELD_BATCH_SIZE ) ) );
        dto.setLoginPrefix( trimOrNull( request.getParameter( FIELD_LOGIN_PREFIX ) ) );
        dto.setLoginSuffix( trimOrNull( request.getParameter( FIELD_LOGIN_SUFFIX ) ) );
        dto.setPassword( trimOrNull( request.getParameter( FIELD_PASSWORD ) ) );
        dto.setFirstNamePrefix( trimOrNull( request.getParameter( FIELD_FIRST_NAME_PREFIX ) ) );
        dto.setFamilyNamePrefix( trimOrNull( request.getParameter( FIELD_FAMILY_NAME_PREFIX ) ) );
        dto.setBirthdate( trimOrNull( request.getParameter( FIELD_BIRTHDATE ) ) );
        dto.setBirthCountryCode( trimOrNull( request.getParameter( FIELD_BIRTH_COUNTRY_CODE ) ) );
        dto.setBirthplaceCode( trimOrNull( request.getParameter( FIELD_BIRTHPLACE_CODE ) ) );
        dto.setIdentityCertifier( trimOrNull( request.getParameter( FIELD_IDENTITY_CERTIFIER ) ) );
        dto.setMailLoginCertifier( trimOrNull( request.getParameter( FIELD_MAIL_LOGIN_CERTIFIER ) ) );
        return dto;
    }

    private static int parseInt( final String value )
    {
        if ( value == null || value.trim( ).isEmpty( ) )
        {
            return 0;
        }
        return Integer.parseInt( value.trim( ) );
    }

    private static String trimOrNull( final String value )
    {
        if ( value == null )
        {
            return null;
        }
        final String trimmed = value.trim( );
        return trimmed.isEmpty( ) ? null : trimmed;
    }
}
