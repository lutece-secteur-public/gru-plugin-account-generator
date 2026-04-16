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
    private static final String PARAMETER_GENERATION_PATTERN = "generation_pattern";
    private static final String PARAMETER_GENERATION_INCREMENT_OFFSET = "generation_increment_offset";
    private static final String PARAMETER_NB_DAYS_OF_VALIDITY = "nb_days_of_validity";
    private static final String PARAMETER_BATCH_SIZE = "batch_size";

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

    // Session state
    private transient List<GeneratedAccountDto> _generatedAccounts;
    private String _strGenerationMessage;

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
        final String strPattern = request.getParameter( PARAMETER_GENERATION_PATTERN );
        final String strOffset = request.getParameter( PARAMETER_GENERATION_INCREMENT_OFFSET );
        final String strNbDays = request.getParameter( PARAMETER_NB_DAYS_OF_VALIDITY );
        final String strBatchSize = request.getParameter( PARAMETER_BATCH_SIZE );

        if ( strPattern == null || strPattern.trim( ).isEmpty( ) || strOffset == null || strNbDays == null || strBatchSize == null )
        {
            addError( ERROR_INVALID_PARAMETERS, getLocale( ) );
            return redirectView( request, VIEW_GENERATE_ACCOUNTS );
        }

        try
        {
            final AccountGenerationDto accountGenerationDto = new AccountGenerationDto( );
            accountGenerationDto.setGenerateAccount( "on".equals( strGenerateAccount ) || "true".equals( strGenerateAccount ) );
            accountGenerationDto.setGenerationPattern( strPattern.trim( ) );
            accountGenerationDto.setGenerationIncrementOffset( Integer.parseInt( strOffset ) );
            accountGenerationDto.setNbDaysOfValidity( Integer.parseInt( strNbDays ) );

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
