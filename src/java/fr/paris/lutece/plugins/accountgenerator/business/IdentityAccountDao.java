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
package fr.paris.lutece.plugins.accountgenerator.business;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class IdentityAccountDao implements IIdentityAccountDao
{

    private final static String INSERT = "INSERT INTO accountgenerator_account(guid, cuid, creationDate, expirationDate) VALUES (?,?,?,?)";
    private final static String SELECT_EXPIRED_ACCOUNTS = "SELECT guid, cuid, creationDate, expirationDate FROM accountgenerator_account WHERE expirationDate <= NOW()";

    @Override
    public void bulkSave( final List<IdentityAccount> accounts, final Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( INSERT ) )
        {
            for ( final IdentityAccount account : accounts )
            {
                daoUtil.setString( 1, account.getGuid( ) );
                daoUtil.setString( 2, account.getCuid( ) );
                daoUtil.setDate( 3, new Date( account.getCreationDate( ).getTime( ) ) );
                daoUtil.setDate( 4, new Date( account.getExpirationDate( ).getTime( ) ) );
                daoUtil.executeUpdate( );
            }
        }
    }

    @Override
    public void save( final IdentityAccount account, final Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( INSERT ) )
        {
            daoUtil.setString( 1, account.getGuid( ) );
            daoUtil.setString( 2, account.getCuid( ) );
            daoUtil.setDate( 3, new Date( account.getCreationDate( ).getTime( ) ) );
            daoUtil.setDate( 4, new Date( account.getExpirationDate( ).getTime( ) ) );
            daoUtil.executeUpdate( );
        }
    }

    @Override
    public List<IdentityAccount> loadExpiredAccounts( final Plugin plugin )
    {
        final List<IdentityAccount> accounts = new ArrayList<>( );
        try ( final DAOUtil daoUtil = new DAOUtil( SELECT_EXPIRED_ACCOUNTS ) )
        {
            daoUtil.executeQuery( );
            while ( daoUtil.next( ) )
            {
                final IdentityAccount account = new IdentityAccount( );
                account.setGuid( daoUtil.getString( 1 ) );
                account.setCuid( daoUtil.getString( 2 ) );
                account.setCreationDate( daoUtil.getDate( 3 ) );
                account.setExpirationDate( daoUtil.getDate( 4 ) );
                accounts.add( account );
            }
        }
        return accounts;
    }
}
