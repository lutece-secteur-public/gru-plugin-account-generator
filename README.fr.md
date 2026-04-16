![](https://dev.lutece.paris.fr/jenkins/buildStatus/icon?job=gru-plugin-account-generator-deploy)
[![Alerte](https://dev.lutece.paris.fr/sonar/api/project_badges/measure?project=fr.paris.lutece.plugins%3Aplugin-accountgenerator&metric=alert_status)](https://dev.lutece.paris.fr/sonar/dashboard?id=fr.paris.lutece.plugins%3Aplugin-accountgenerator)
[![Line of code](https://dev.lutece.paris.fr/sonar/api/project_badges/measure?project=fr.paris.lutece.plugins%3Aplugin-accountgenerator&metric=ncloc)](https://dev.lutece.paris.fr/sonar/dashboard?id=fr.paris.lutece.plugins%3Aplugin-accountgenerator)
[![Coverage](https://dev.lutece.paris.fr/sonar/api/project_badges/measure?project=fr.paris.lutece.plugins%3Aplugin-accountgenerator&metric=coverage)](https://dev.lutece.paris.fr/sonar/dashboard?id=fr.paris.lutece.plugins%3Aplugin-accountgenerator)

# Plugin accountgenerator

## Introduction

Le plugin `plugin-accountgenerator` permet la création automatisée en lot et la gestion du cycle de vie des identités et des comptes MonParis. Il est destinéaux environnements de test et de pré-production pour générer rapidement des données utilisateurs réalistes avec des attributs certifiés (nom, date de naissance, genre, email, lieu de naissance, etc.).

Le plugin propose deux moyens de déclencher la génération de comptes :

 
* Une **API REST** pour une intégration programmatique ou via des outils externes.
* Une **fonctionnalitéd'administration Back Office** avec un formulaire pour lancer la génération directement depuis l'interface d'administration Lutece.

Les comptes générés sont enregistrés dans une table de base de données avec une date d'expiration. Un daemon configurable purge automatiquement les comptes expirés ainsi que les identités et comptes MonParis associés via les API externes.

Le plugin s'intègre avec trois API externes :

 
*  **API IdentityStore** - pour créer et supprimer des identités avec des attributs certifiés.
*  **API AccountManagement** - pour créer, valider et supprimer des comptes MonParis.
*  **API Geocodes** - pour récupérer les codes communes valides pour les attributs de lieu de naissance (avec cache).

## Configuration

 **Propriétés** 

Le fichier `accountgenerator.properties` dans `webapp/WEB-INF/conf/plugins/` contient toutes les propriétés configurables :

| Propriété| Valeur par défaut| Description|
|-----------------|-----------------|-----------------|
|  `accountgenerator.identitystore.ApiEndPointUrl` | http://localhost:8080| URL de base de l'API IdentityStore|
|  `accountgenerator.accountManagement.ApiEndPointUrl` | http://localhost:9080| URL de base de l'API AccountManagement|
|  `accountgenerator.accountManagement.path` | /site-openam/rest/openam/api/1| Chemin de l'API AccountManagement|
|  `accountgenerator.accountManagement.client-code` | TEST| Code client pour les appelsàl'API IdentityStore|
|  `accountgenerator.accountManagement.client-name` | AccountGenerator| Nom du client pour les appelsàl'API IdentityStore|
|  `accountgenerator.accountManagement.client-id` | test| Identifiant client pour l'API AccountManagement|
|  `accountgenerator.accountManagement.secret-id` | test| Secret client pour l'API AccountManagement|
|  `accountgenerator.geocodes.city.codes.endpoint` | http://localhost:8080/rest/geocodes/api/v1/cities/codes| Endpoint de l'API Geocodes pour les codes communes|
|  `accountgenerator.generation.limit` | 50| Nombre maximum de comptes par requête de génération|
|  `accountgenerator.generation.password` | password123456789| Mot de passe commun attribuéàtous les comptes générés|
|  `accountgenerator.generation.mail.suffix` | @paris.test.fr| Suffixe email pour les comptes générés|
|  `accountgenerator.generation.certifier.*` | fccertifier| Code du certificateur pour chaque attribut d'identité(first_name, family-name, birthplace_code, birthcountry_code, gender, birthdate, email, login)|
|  `accountgenerator.generation.value.birthcountry_code` | 99100| Code pays de naissance (99100 = France)|
|  `accountgenerator.generation.value.max.birthdate.year` | 2000| Année maximale pour la génération aléatoire de la date de naissance|
|  `accountgenerator.generation.value.max.birthdate.month` | 12| Mois maximum pour la génération aléatoire de la date de naissance|
|  `accountgenerator.generation.value.max.birthdate.day` | 31| Jour maximum pour la génération aléatoire de la date de naissance|

 **Beans Spring** 

Le contexte Spring est défini dans `accountgenerator_context.xml` . Les beans suivants sont déclarés :

| ID du bean| Classe| Description|
|-----------------|-----------------|-----------------|
|  `accountgenerator.identityService` | IdentityService| Service client pour l'API IdentityStore|
|  `accountgenerator.accountManagementService` | AccountManagementService| Service client pour l'API AccountManagement|
|  `accountgenerator.geocodesCache` | GeocodesCache| Service de cache pour les codes communes de l'API Geocodes|
|  `accountgenerator.accountDao` | IdentityAccountDao| DAO pour la table accountgenerator_account|

 **Daemons** 

Le plugin déclare un daemon :

| ID du daemon| Classe| Description|
|-----------------|-----------------|-----------------|
|  `purgeExpiratedIdentityAccountsDaemons` | PurgeExpiratedIdentityAccountsDaemons| Purge périodiquement les comptes générés expirés ainsi que les identités associées en appelant les API IdentityStore et AccountManagement.|

 **Caches** 

| Nom du cache| Classe| Description|
|-----------------|-----------------|-----------------|
|  `AccountGeneratorGeocodesCache` | GeocodesCache| Met en cache les codes communes récupérés depuis l'API Geocodes, indexés par date de référence.Évite les appels HTTP répétés pour la génération des codes de lieu de naissance.|

## Usage

 **Droits d'administration** 

| Clédu droit| Description| URL d'administration|
|-----------------|-----------------|-----------------|
|  `ACCOUNTGENERATOR_MANAGEMENT` | Droit d'accèsàla fonctionnalitéde génération de comptes en Back Office| jsp/admin/plugins/accountgenerator/AccountGeneratorManagement.jsp|

 **Services** 

| Service| Méthode| Description|
|-----------------|-----------------|-----------------|
|  `IdentityAccountGeneratorService` |  `createIdentityAccountBatch(AccountGenerationDto)` | Crée un lot d'identités avec des attributs certifiés aléatoires et crée optionnellement des comptes MonParis associés. Retourne une liste de GeneratedAccountDto contenant email, mot de passe, cuid, guid et messages de statut pour chaque compte.|
|  `IdentityAccountPurgeService` |  `purge()` | Charge les comptes expirés depuis la base de données et supprime les comptes MonParis et identités associés via les API externes. Retourne des statistiques de purge.|

 **API REST** 

| Verbe HTTP| Chemin| Description|
|-----------------|-----------------|-----------------|
| POST|  `/rest/identitystore/api/v3/account/generator` | Générer un lot d'identités et/ou de comptes MonParis|

 **En-têtes de la requête :** 

 
*  `X-Client-Code` - code de l'application cliente
*  `X-Author-Name` - nom de l'auteur
*  `X-Author-Type` - type de l'auteur
*  `X-Application-Code` - code de l'application

 **Corps de la requête (JSON) :** 

| Champ| Type| Description|
|-----------------|-----------------|-----------------|
|  `generation.generateAccount` | boolean| Indique s'il fautégalement créer un compte MonParis pour chaque identité|
|  `generation.generationPattern` | string| Motif utilisédans les valeurs des attributs générés (email, noms)|
|  `generation.generationIncrementOffset` | integer| Offset ajoutéau compteur d'itération pour la génération des attributs|
|  `generation.nbDaysOfValidity` | integer| Nombre de jours avant l'expiration des comptes générés|
|  `generation.batchSize` | integer| Nombre d'identités/comptesàgénérer (plafonnépar la propriétégeneration.limit)|

 **Corps de la réponse (JSON) :** Un objet `AccountGenerationResponse` contenant une liste `generated_accounts` . Chaque entrée comprend `email` , `password` , `cuid` , `guid` , et une liste `status` de messages. La réponse est synchrone.


[Maven documentation and reports](https://dev.lutece.paris.fr/plugins/plugin-accountgenerator/)



 *generated by [xdoc2md](https://github.com/lutece-platform/tools-maven-xdoc2md-plugin) - do not edit directly.*