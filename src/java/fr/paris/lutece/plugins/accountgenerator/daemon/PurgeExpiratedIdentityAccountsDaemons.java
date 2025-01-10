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
package fr.paris.lutece.plugins.accountgenerator.daemon;

import fr.paris.lutece.plugins.accountgenerator.dto.PurgeExipratedIdentityAccountsResult;
import fr.paris.lutece.plugins.accountgenerator.service.IdentityAccountPurgeService;
import fr.paris.lutece.portal.service.daemon.Daemon;
import fr.paris.lutece.portal.service.util.AppLogService;

import java.time.Duration;
import java.time.LocalDateTime;

public class PurgeExpiratedIdentityAccountsDaemons extends Daemon
{

    @Override
    public void run( )
    {
        final LocalDateTime start = LocalDateTime.now( );
        AppLogService.info( "Purge Expirated Identity Accounts daemon started" );
        final PurgeExipratedIdentityAccountsResult purge = IdentityAccountPurgeService.instance( ).purge( );
        final LocalDateTime stop = LocalDateTime.now( );
        final long seconds = Duration.between( start, stop ).toSeconds( );

        // Calculate the hours, minutes, and seconds
        long S = seconds % 60; // Calculate the remaining seconds
        long H = seconds / 60; // Convert total seconds to minutes
        long M = H % 60; // Calculate the remaining minutes
        H = H / 60; // Convert total minutes to hours

        AppLogService.info( purge );
        AppLogService.info( "Purge Expirated Identity Accounts daemon ended in " + H + ":" + M + ":" + S );
    }
}
