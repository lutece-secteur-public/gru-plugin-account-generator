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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.paris.lutece.plugins.accountgenerator.dto.CitiesCodesListResponse;
import fr.paris.lutece.portal.service.cache.AbstractCacheableService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class GeocodesCache extends AbstractCacheableService
{
    private static final String CONST_PREFIX_CITY_CODES = "city_codes_";

    private static final String geocodesCitiesCodesEndpoint = AppPropertiesService.getProperty( "accountgenerator.geocodes.city.codes.endpoint" );
    private static final SimpleDateFormat citiesCodesDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
    private static final HttpClient client = HttpClient.newHttpClient( );
    private static final ObjectMapper _mapper = new ObjectMapper( );

    private static GeocodesCache instance;

    public static GeocodesCache instance( )
    {
        if ( instance == null )
        {
            instance = new GeocodesCache( );
        }
        return instance;
    }

    private GeocodesCache( )
    {
        AppLogService.debug( "Init GeocodesCache cache" );
        _mapper.disable( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES );
        _mapper.enable( DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT );
        _mapper.enable( DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT );
        this.resetCache( );
    }

    public List<String> getCodesFromCache( final Date date )
    {
        final String strDate = citiesCodesDateFormat.format( date );
        final String key = CONST_PREFIX_CITY_CODES + strDate;
        try
        {
            final Object o = this.getFromCache( key );
            if ( o == null || ( (List<String>) o ).isEmpty( ) )
            {
                final URI uri = new URI( geocodesCitiesCodesEndpoint + "?dateref=" + strDate );
                final HttpRequest getGeocodesCodes = HttpRequest.newBuilder( uri ).GET( ).build( );
                final String response = client.send( getGeocodesCodes, HttpResponse.BodyHandlers.ofString( ) ).body( );
                final CitiesCodesListResponse citiesCodesListResponse = _mapper.readValue( response, CitiesCodesListResponse.class );
                if ( citiesCodesListResponse != null && Objects.equals( citiesCodesListResponse.getStatus( ), "OK" ) )
                {
                    this.putInCache( key, citiesCodesListResponse.getResult( ) );
                    return citiesCodesListResponse.getResult( );
                }
                return new ArrayList<>( );
            }
            else
            {
                return (List<String>) o;
            }
        }
        catch( final Exception e )
        {
            return new ArrayList<>( );
        }
    }

    @Override
    public String getName( )
    {
        return "AccountGeneratorGeocodesCache";
    }
}
