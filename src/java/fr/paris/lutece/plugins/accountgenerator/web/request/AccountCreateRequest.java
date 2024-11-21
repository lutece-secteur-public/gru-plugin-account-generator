package fr.paris.lutece.plugins.accountgenerator.web.request;

import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.account.AccountRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.account.AccountResponse;
import fr.paris.lutece.plugins.accountgenerator.service.AccountService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.AbstractIdentityStoreRequest;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;

public class AccountCreateRequest extends AbstractIdentityStoreRequest
{

    private final AccountRequest _accountRequest;
    private final String _strHeaderAppCode;

    public AccountCreateRequest(AccountRequest request, String strHeaderAppCode, String strClientCode, String authorName, String authorType) throws IdentityStoreException
    {
        super(strClientCode, authorName, authorType);
        _accountRequest = request;
        _strHeaderAppCode = strHeaderAppCode;
    }

    @Override
    protected void validateSpecificRequest() throws IdentityStoreException
    {

    }

    @Override
    protected AccountResponse doSpecificRequest() throws IdentityStoreException
    {
        final AccountResponse response = new AccountResponse();

        response.setAccountList(AccountService.instance().createAccountBatch(_accountRequest.getAccountDto(), this._author, this._strClientCode));

        return response;
    }
}
