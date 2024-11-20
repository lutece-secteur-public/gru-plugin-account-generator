package fr.paris.lutece.plugins.accountgenerator.web.rs;

import fr.paris.lutece.plugins.accountgenerator.web.request.AccountCreateRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.account.AccountRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.rest.service.RestConstants;
import org.springframework.web.bind.annotation.RequestBody;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path( RestConstants.BASE_PATH + Constants.PLUGIN_PATH + Constants.VERSION_PATH_V3 + Constants.GENERATOR_PATH + Constants.ACCOUNT_GENERATOR_PATH )
public class AccountRestService
{

    @POST
    @Path( Constants.GENERATE_PATH )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    public Response generateAccount(@RequestBody final AccountRequest accountRequest, @HeaderParam( Constants.PARAM_CLIENT_CODE ) String strHeaderClientCode,
                                    @HeaderParam( Constants.PARAM_AUTHOR_NAME ) String authorName, @HeaderParam( Constants.PARAM_AUTHOR_TYPE ) String authorType,
                                    @HeaderParam( Constants.PARAM_APPLICATION_CODE ) String strHeaderAppCode ) throws IdentityStoreException
    {
        final AccountCreateRequest request = new AccountCreateRequest( accountRequest, strHeaderAppCode, strHeaderClientCode, authorName, authorType );
        return buildJsonResponse((ResponseDto) request.doRequest());
    }

    private Response buildJsonResponse( final ResponseDto entity)
    {
        return Response.status( entity.getStatus( ).getHttpCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
    }
}
