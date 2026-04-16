![](https://dev.lutece.paris.fr/jenkins/buildStatus/icon?job=gru-plugin-account-generator-deploy)
[![Alerte](https://dev.lutece.paris.fr/sonar/api/project_badges/measure?project=fr.paris.lutece.plugins%3Aplugin-accountgenerator&metric=alert_status)](https://dev.lutece.paris.fr/sonar/dashboard?id=fr.paris.lutece.plugins%3Aplugin-accountgenerator)
[![Line of code](https://dev.lutece.paris.fr/sonar/api/project_badges/measure?project=fr.paris.lutece.plugins%3Aplugin-accountgenerator&metric=ncloc)](https://dev.lutece.paris.fr/sonar/dashboard?id=fr.paris.lutece.plugins%3Aplugin-accountgenerator)
[![Coverage](https://dev.lutece.paris.fr/sonar/api/project_badges/measure?project=fr.paris.lutece.plugins%3Aplugin-accountgenerator&metric=coverage)](https://dev.lutece.paris.fr/sonar/dashboard?id=fr.paris.lutece.plugins%3Aplugin-accountgenerator)

# Plugin accountgenerator

## Introduction

The `plugin-accountgenerator` plugin provides automated batch creation and lifecycle management of user identities and MonParis accounts. It is designed for test and pre-production environments to quicklygenerate realistic user data with certified attributes (name, birthdate, gender, email, birthplace, etc.).

The plugin offers two ways to trigger account generation:

 
* A **REST API** endpoint for programmatic or external tool integration.
* A **Back Office admin feature** with a form to launch generation directly from the Lutece administration interface.

Generated accounts are tracked in a database table with an expiration date. A configurable daemon automatically purges expired accounts and their associated identities and MonParis accounts via external APIs.

The plugin integrates with three external APIs:

 
*  **IdentityStore API** - to create and delete identities with certified attributes.
*  **AccountManagement API** - to create, validate, and delete MonParis accounts.
*  **Geocodes API** - to fetch valid city codes for birthplace attributes (cached).

## Configuration

 **Properties** 

The file `accountgenerator.properties` in `webapp/WEB-INF/conf/plugins/` contains all configurable properties:

| Property| Default value| Description|
|-----------------|-----------------|-----------------|
|  `accountgenerator.identitystore.ApiEndPointUrl` | http://localhost:8080| IdentityStore API base URL|
|  `accountgenerator.accountManagement.ApiEndPointUrl` | http://localhost:9080| AccountManagement API base URL|
|  `accountgenerator.accountManagement.path` | /site-openam/rest/openam/api/1| AccountManagement API path|
|  `accountgenerator.accountManagement.client-code` | TEST| Client code for IdentityStore API calls|
|  `accountgenerator.accountManagement.client-name` | AccountGenerator| Client name for IdentityStore API calls|
|  `accountgenerator.accountManagement.client-id` | test| Client ID for AccountManagement API|
|  `accountgenerator.accountManagement.secret-id` | test| Client secret for AccountManagement API|
|  `accountgenerator.geocodes.city.codes.endpoint` | http://localhost:8080/rest/geocodes/api/v1/cities/codes| Geocodes API endpoint for city codes|
|  `accountgenerator.generation.limit` | 50| Maximum number of accounts per generation request|
|  `accountgenerator.generation.password` | password123456789| Common password assigned to all generated accounts|
|  `accountgenerator.generation.mail.suffix` | @paris.test.fr| Email suffix for generated accounts|
|  `accountgenerator.generation.certifier.*` | fccertifier| Certifier code for each identity attribute (first_name, family-name, birthplace_code, birthcountry_code, gender, birthdate, email, login)|
|  `accountgenerator.generation.value.birthcountry_code` | 99100| Birth country code (99100 = France)|
|  `accountgenerator.generation.value.max.birthdate.year` | 2000| Maximum year for random birthdate generation|
|  `accountgenerator.generation.value.max.birthdate.month` | 12| Maximum month for random birthdate generation|
|  `accountgenerator.generation.value.max.birthdate.day` | 31| Maximum day for random birthdate generation|

 **Spring Beans** 

The Spring context is defined in `accountgenerator_context.xml` . The following beans are declared:

| Bean ID| Class| Description|
|-----------------|-----------------|-----------------|
|  `accountgenerator.identityService` | IdentityService| Client service for the IdentityStore API|
|  `accountgenerator.accountManagementService` | AccountManagementService| Client service for the AccountManagement API|
|  `accountgenerator.geocodesCache` | GeocodesCache| Cache service for city codes from the Geocodes API|
|  `accountgenerator.accountDao` | IdentityAccountDao| DAO for the accountgenerator_account table|

 **Daemons** 

The plugin declares one daemon:

| Daemon ID| Class| Description|
|-----------------|-----------------|-----------------|
|  `purgeExpiratedIdentityAccountsDaemons` | PurgeExpiratedIdentityAccountsDaemons| Periodically purges expired generated accounts and their associated identities by calling the IdentityStore and AccountManagement APIs.|

 **Caches** 

| Cache name| Class| Description|
|-----------------|-----------------|-----------------|
|  `AccountGeneratorGeocodesCache` | GeocodesCache| Caches city codes fetched from the Geocodes API, keyed by date reference. Avoids repeated HTTP calls for birthplace code generation.|

## Usage

 **Admin Rights** 

| Right key| Description| Admin URL|
|-----------------|-----------------|-----------------|
|  `ACCOUNTGENERATOR_MANAGEMENT` | Right to access the account generation back office feature| jsp/admin/plugins/accountgenerator/AccountGeneratorManagement.jsp|

 **Services** 

| Service| Method| Description|
|-----------------|-----------------|-----------------|
|  `IdentityAccountGeneratorService` |  `createIdentityAccountBatch(AccountGenerationDto)` | Creates a batch of identities with random certified attributes and optionally creates associated MonParis accounts. Returns a list of GeneratedAccountDto with email, password, cuid, guid, and status messages for each account.|
|  `IdentityAccountPurgeService` |  `purge()` | Loads expired accounts from the database and deletes associated MonParis accounts and identities via external APIs. Returns purge statistics.|

 **REST API** 

| HTTP verb| Path| Description|
|-----------------|-----------------|-----------------|
| POST|  `/rest/identitystore/api/v3/account/generator` | Generate a batch of identities and/or MonParis accounts|

 **Request headers:** 

 
*  `X-Client-Code` - client application code
*  `X-Author-Name` - author name
*  `X-Author-Type` - author type
*  `X-Application-Code` - application code

 **Request body (JSON):** 

| Field| Type| Description|
|-----------------|-----------------|-----------------|
|  `generation.generateAccount` | boolean| Whether to also create a MonParis account for each identity|
|  `generation.generationPattern` | string| Pattern used in generated attribute values (email, names)|
|  `generation.generationIncrementOffset` | integer| Offset added to the iteration counter for attribute generation|
|  `generation.nbDaysOfValidity` | integer| Number of days before the generated accounts expire|
|  `generation.batchSize` | integer| Number of identities/accounts to generate (capped by generation.limit property)|

 **Response body (JSON):** An `AccountGenerationResponse` containing a `generated_accounts` list. Each entry includes `email` , `password` , `cuid` , `guid` , and a `status` list of messages. The response is synchronous.


[Maven documentation and reports](https://dev.lutece.paris.fr/plugins/plugin-accountgenerator/)



 *generated by [xdoc2md](https://github.com/lutece-platform/tools-maven-xdoc2md-plugin) - do not edit directly.*