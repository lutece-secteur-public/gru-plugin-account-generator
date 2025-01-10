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
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.account.generator.GeneratedAccountDto;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class AccountGenerationServiceTest
{

    public void testAccountGenerationWithoutMonParis( )
    {
        final AccountGenerationDto accountGenerationDto = new AccountGenerationDto( );
        accountGenerationDto.setGenerationIncrementOffset( 0 );
        accountGenerationDto.setGenerationPattern( "XYZ" );
        accountGenerationDto.setBatchSize( 2 );
        accountGenerationDto.setNbDaysOfValidity( 5 );
        accountGenerationDto.setGenerateAccount( false );

        // TODO Test
        final List<GeneratedAccountDto> result = null;

        assertEquals( result.size( ), 2 );

        assertEquals( result.get( 0 ).getEmail( ), "user.XYZ.1@paris.test.fr" );
        assertEquals( result.get( 0 ).getPassword( ), "user.XYZ.1" );
        assertNotNull( result.get( 0 ).getCuid( ) );
        assertNull( result.get( 0 ).getGuid( ) );

        assertEquals( result.get( 1 ).getEmail( ), "user.XYZ.2@paris.test.fr" );
        assertEquals( result.get( 1 ).getPassword( ), "user.XYZ.2" );
        assertNotNull( result.get( 1 ).getCuid( ) );
        assertNull( result.get( 1 ).getGuid( ) );
    }

    public void testAccountGenerationWithMonParis( )
    {
        AccountGenerationDto accountGenerationDto = new AccountGenerationDto( );
        accountGenerationDto.setGenerationIncrementOffset( 0 );
        accountGenerationDto.setGenerationPattern( "XYZ" );
        accountGenerationDto.setBatchSize( 2 );
        accountGenerationDto.setNbDaysOfValidity( 5 );
        accountGenerationDto.setGenerateAccount( true );

        // TODO test
        List<GeneratedAccountDto> result = null;

        // assert
        assertEquals( result.size( ), 2 );

        assertEquals( result.get( 0 ).getEmail( ), "user.XYZ.1@paris.test.fr" );
        assertEquals( result.get( 0 ).getPassword( ), "user.XYZ.1" );
        assertNotNull( result.get( 0 ).getCuid( ) );
        assertNotNull( result.get( 0 ).getGuid( ) );

        assertEquals( result.get( 1 ).getEmail( ), "user.XYZ.2@paris.test.fr" );
        assertEquals( result.get( 1 ).getPassword( ), "user.XYZ.2" );
        assertNotNull( result.get( 1 ).getCuid( ) );
        assertNotNull( result.get( 1 ).getGuid( ) );
    }
}
