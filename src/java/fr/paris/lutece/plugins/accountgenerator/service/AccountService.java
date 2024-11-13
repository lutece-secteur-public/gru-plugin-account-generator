package fr.paris.lutece.plugins.accountgenerator.service;

import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.account.AccountCreationDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.account.AccountDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ExpirationDefinition;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.RequestAuthor;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchAttribute;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchDto;
import fr.paris.lutece.plugins.identitystore.v3.web.service.IdentityService;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.spring.SpringContextService;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AccountService
{
    private static AccountService instance;

    private static final IdentityService _identityService = SpringContextService.getBean("accountgenerator.identityService");
    private static final String MAIL = "@paris.test.fr";
    private static final String MAIL_KEY = "12";
    private static final String LOGIN_KEY = "16";
    private static final int CERTIFICATION_LEVEL = 600;

    public static AccountService instance( )
    {
        if ( instance == null )
        {
            instance = new AccountService( );
        }
        return instance;
    }

    public List<AccountDto> createAccountBatch(AccountCreationDto accountCreationDto, RequestAuthor author, String clientCode)
    {
        List<AccountDto> accounts = new ArrayList<AccountDto>( );

        if(accountCreationDto != null)
        {
            for (int i = 1 ; i <= accountCreationDto.getBatchSize(); i++)
            {
                AccountDto account = new AccountDto( );
                String password = accountCreationDto.getPattern()
                        .concat(".")
                        .concat(accountCreationDto.getPattern())
                        .concat(".")
                        .concat(String.valueOf(i));
                String mail = password.concat(MAIL);
                if (accountCreationDto.isMonParis())
                {
                    try
                    {
                        //TODO appel à l'API de création de comptes et récupération du GUID
                        //account.setGuid();
                        account.setPassword(password);
                    } catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                }
                IdentityChangeRequest identityChange = buildIdentityChangeRequest(accountCreationDto, i, mail);
                IdentitySearchRequest identitySearchRequest = buildSearchRequest(mail);
                try
                {
                     _identityService.createIdentity(identityChange, clientCode, author);
                    IdentitySearchResponse identitySearchResponse = _identityService.searchIdentities(identitySearchRequest, clientCode, author);
                    account.setCuid(identitySearchResponse.getIdentities().get(0).getCustomerId());
                } catch (IdentityStoreException e)
                {
                    throw new RuntimeException(e);
                }
                account.setEmail(mail);
                accounts.add(account);
            }
        }

        return accounts;
    }

    public IdentityChangeRequest buildIdentityChangeRequest( AccountCreationDto accountCreationDto, int index,
                                                             String mail )
    {
        IdentityChangeRequest identityChangeRequest = new IdentityChangeRequest( );
        IdentityDto identityDto = new IdentityDto( );
        identityDto.setMonParisActive(accountCreationDto.isMonParis());

        Timestamp now = new Timestamp( System.currentTimeMillis( ) );
        identityDto.setLastUpdateDate(now);
        identityDto.setCreationDate(now);

        ExpirationDefinition expirationDefinition = new ExpirationDefinition( );
        expirationDefinition.setExpirationDate(Timestamp.valueOf(now.toLocalDateTime().plusDays(accountCreationDto.getValidityTime())));
        identityDto.setExpiration(expirationDefinition);

        identityChangeRequest.getIdentity().getAttributes().add(buildAttribute(mail, MAIL_KEY));
        identityChangeRequest.getIdentity().getAttributes().add(buildAttribute(mail, LOGIN_KEY));


        identityChangeRequest.setIdentity(identityDto);
        return identityChangeRequest;
    }

    public AttributeDto buildAttribute (String key, String value)
    {
        AttributeDto attribute = new AttributeDto();

        attribute.setValue(value);
        attribute.setKey(key);
        attribute.setCertificationLevel(CERTIFICATION_LEVEL);
        attribute.setCertificationLevel(CERTIFICATION_LEVEL);
        attribute.setLastUpdateDate(new Timestamp( System.currentTimeMillis( ) ) );
        attribute.setCertificationDate(new Timestamp( System.currentTimeMillis( ) ) );

        return attribute;
    }

    public IdentitySearchRequest buildSearchRequest(String mail)
    {
        IdentitySearchRequest identitySearchRequest = new IdentitySearchRequest( );
        SearchDto searchDto = new SearchDto( );
        searchDto.setAttributes(new ArrayList<SearchAttribute>() );
        SearchAttribute searchAttribute = new SearchAttribute( );
        searchAttribute.setKey(LOGIN_KEY);
        searchAttribute.setValue(mail);
        searchDto.getAttributes().add(searchAttribute);
        identitySearchRequest.setSearch(searchDto);
        return identitySearchRequest;
    }

}
