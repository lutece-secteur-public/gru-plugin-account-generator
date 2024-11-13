package fr.paris.lutece.plugins.accountgenerator.web.request;

import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.account.AccountRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.account.AccountResponse;
import fr.paris.lutece.plugins.accountgenerator.service.AccountService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.AbstractIdentityStoreRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseDto;
import fr.paris.lutece.plugins.identitystore.web.exception.ClientAuthorizationException;
import fr.paris.lutece.plugins.identitystore.web.exception.DuplicatesConsistencyException;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestContentFormattingException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestFormatException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceConsistencyException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceNotFoundException;

public class AccountCreateRequest extends AbstractIdentityStoreRequest
{

    private final AccountRequest _accountRequest;
    private final String _strHeaderAppCode;

    public AccountCreateRequest(AccountRequest request, String strHeaderAppCode, String strClientCode, String authorName, String authorType) throws RequestFormatException
    {
        super(strClientCode, authorName, authorType);
        _accountRequest = request;
        _strHeaderAppCode = strHeaderAppCode;
    }

    @Override
    protected void fetchResources() throws ResourceNotFoundException
    {

    }

    @Override
    protected void validateRequestFormat() throws RequestFormatException
    {

    }

    @Override
    protected void validateClientAuthorization() throws ClientAuthorizationException
    {

    }

    @Override
    protected void validateResourcesConsistency() throws ResourceConsistencyException
    {

    }

    @Override
    protected void formatRequestContent() throws RequestContentFormattingException
    {

    }

    @Override
    protected void checkDuplicatesConsistency() throws DuplicatesConsistencyException
    {

    }

    @Override
    protected ResponseDto doSpecificRequest() throws IdentityStoreException
    {
        final AccountResponse response = new AccountResponse();

        response.setAccountList(AccountService.instance().createAccountBatch(_accountRequest.getAccoutDto(), this._author, this._strClientCode));

        return response;
    }
}
