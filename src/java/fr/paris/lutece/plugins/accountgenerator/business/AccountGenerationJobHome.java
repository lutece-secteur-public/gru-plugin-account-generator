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

import fr.paris.lutece.plugins.accountgenerator.AccountGeneratorPlugin;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;

import java.util.List;
import java.util.Optional;

public final class AccountGenerationJobHome
{
    private static final IAccountGenerationJobDao _dao = SpringContextService.getBean( IAccountGenerationJobDao.BEAN_NAME );
    private static final Plugin _plugin = PluginService.getPlugin( AccountGeneratorPlugin.PLUGIN_NAME );

    private AccountGenerationJobHome( )
    {
    }

    public static void create( final AccountGenerationJob job )
    {
        _dao.insert( job, _plugin );
    }

    public static void update( final AccountGenerationJob job )
    {
        _dao.update( job, _plugin );
    }

    public static Optional<AccountGenerationJob> findByReference( final String strReference )
    {
        return _dao.loadByReference( strReference, _plugin );
    }

    public static Optional<AccountGenerationJob> findById( final int nId )
    {
        return _dao.loadById( nId, _plugin );
    }

    public static List<AccountGenerationJob> findAll( )
    {
        return _dao.loadAll( _plugin );
    }
}
