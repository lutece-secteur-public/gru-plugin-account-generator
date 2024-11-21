package fr.paris.lutece.plugins.accountgenerator.service;

import com.google.errorprone.annotations.DoNotMock;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.account.AccountCreationDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.account.AccountDto;
import fr.paris.lutece.plugins.identitystore.v3.web.service.IdentityService;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class AccountServiceTest
{

    public void testAccountGenerationWithoutMonParis ()
    {
        AccountCreationDto accountCreationDto = new AccountCreationDto( );
        accountCreationDto.setUserName("user");
        accountCreationDto.setPattern("XYZ");
        accountCreationDto.setBatchSize(2);
        accountCreationDto.setValidityTime(5);
        accountCreationDto.setMonParis(false);

        //TODO Test
        List<AccountDto> result = null;

        assertEquals(result.size(), 2);

        assertEquals(result.get(0).getEmail(), "user.XYZ.1@paris.test.fr");
        assertEquals(result.get(0).getPassword(), "user.XYZ.1");
        assertNotNull(result.get(0).getCuid());
        assertNull(result.get(0).getGuid());

        assertEquals(result.get(1).getEmail(), "user.XYZ.2@paris.test.fr");
        assertEquals(result.get(1).getPassword(), "user.XYZ.2");
        assertNotNull(result.get(1).getCuid());
        assertNull(result.get(1).getGuid());
    }

    public void testAccountGenerationWithMonParis ()
    {
        AccountCreationDto accountCreationDto = new AccountCreationDto( );
        accountCreationDto.setUserName("user");
        accountCreationDto.setPattern("XYZ");
        accountCreationDto.setBatchSize(2);
        accountCreationDto.setValidityTime(5);
        accountCreationDto.setMonParis(true);

        //TODO test
        List<AccountDto> result = null;


        //assert
        assertEquals(result.size(), 2);

        assertEquals(result.get(0).getEmail(), "user.XYZ.1@paris.test.fr");
        assertEquals(result.get(0).getPassword(), "user.XYZ.1");
        assertNotNull(result.get(0).getCuid());
        assertNotNull(result.get(0).getGuid());

        assertEquals(result.get(1).getEmail(), "user.XYZ.2@paris.test.fr");
        assertEquals(result.get(1).getPassword(), "user.XYZ.2");
        assertNotNull(result.get(1).getCuid());
        assertNotNull(result.get(1).getGuid());
    }
}
