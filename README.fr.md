![](https://dev.lutece.paris.fr/jenkins/buildStatus/icon?job=gru-plugin-account-generator-deploy)
[![Alerte](https://dev.lutece.paris.fr/sonar/api/project_badges/measure?project=fr.paris.lutece.plugins%3Aplugin-accountgenerator&metric=alert_status)](https://dev.lutece.paris.fr/sonar/dashboard?id=fr.paris.lutece.plugins%3Aplugin-accountgenerator)
[![Line of code](https://dev.lutece.paris.fr/sonar/api/project_badges/measure?project=fr.paris.lutece.plugins%3Aplugin-accountgenerator&metric=ncloc)](https://dev.lutece.paris.fr/sonar/dashboard?id=fr.paris.lutece.plugins%3Aplugin-accountgenerator)
[![Coverage](https://dev.lutece.paris.fr/sonar/api/project_badges/measure?project=fr.paris.lutece.plugins%3Aplugin-accountgenerator&metric=coverage)](https://dev.lutece.paris.fr/sonar/dashboard?id=fr.paris.lutece.plugins%3Aplugin-accountgenerator)

# Plugin accountgenerator

## Introduction

Le plugin `plugin-accountgenerator` permet la création automatisée en lot et la gestion du cycle de vie des identités et des comptes MonParis. Il est destiné aux environnements de test et de pré-production pour générer rapidement des données utilisateurs réalistes avec des attributs certifiés (nom, date de naissance, genre, email, lieu de naissance, etc.).

Chaque demande de génération est persistée comme un **job** avec une référence (UUID). Les jobs sont traités en asynchrone par un exécuteur en arrière-plan afin que les lots importants (jusqu'à 100 000 comptes) ne bloquent pas l'appelant. La progression est exposée via le `ProgressManagerService` de Lutece ainsi que par un endpoint REST de consultation. Une fois le job terminé, les données générées (email, mot de passe, CUID, GUID) sont mises à disposition sous forme de fichier CSV téléchargeable.

Deux moyens permettent de soumettre un job :

* Une **API REST** : `POST` pour soumettre un job, `GET` pour consulter son état par référence.
* Une **fonctionnalité Back Office** avec trois vues (liste des jobs, formulaire de soumission, détail du job avec barre de progression et lien de téléchargement).

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
|  `accountgenerator.identitystore.ApiEndPointUrl` | | URL de base de l'API IdentityStore|
|  `accountgenerator.identitystore.accessManagerEndPointUrl` | | URL du gestionnaire d'accès pour les appels IdentityStore|
|  `accountgenerator.identitystore.accessManagerCredentials` | | Identifiants du gestionnaire d'accès pour les appels IdentityStore|
|  `accountgenerator.accountManagement.ApiEndPointUrl` | | URL de base de l'API AccountManagement|
|  `accountgenerator.accountManagement.path` | | Chemin de l'API AccountManagement|
|  `accountgenerator.accountManagement.client-code` | | Code client pour les appels à l'API IdentityStore|
|  `accountgenerator.accountManagement.client-name` | | Nom du client pour les appels à l'API IdentityStore|
|  `accountgenerator.accountManagement.client-id` | | Identifiant client pour l'API AccountManagement|
|  `accountgenerator.accountManagement.secret-id` | | Secret client pour l'API AccountManagement|
|  `accountgenerator.geocodes.city.codes.endpoint` | | Endpoint de l'API Geocodes pour les codes communes|
|  `accountgenerator.generation.limit` | 100000| Taille maximale de lot acceptée par une soumission de job|
|  `accountgenerator.generation.password` | password123456789| Mot de passe par défaut attribué à tous les comptes générés quand la requête n'en précise pas|
|  `accountgenerator.generation.mail.suffix` | @paris.test.fr| Suffixe email pour les comptes générés (format légacy)|
|  `accountgenerator.executor.threads` | 2| Taille du pool de threads exécutant les jobs en asynchrone|
|  `accountgenerator.filesystem.path` | /tmp/accountgenerator| Répertoire d'écriture des fichiers CSV générés. Doit être accessible en écriture par le serveur d'application.|
|  `accountgenerator.generation.file.flush.size` | 10| Nombre de comptes entre deux flush CSV et deux lignes de rapport de progression|
|  `accountgenerator.generation.db.flush.size` | 500| Nombre d'identity-accounts bufferisés avant un insert en base|
|  `accountgenerator.generation.csv.separator` | ;| Séparateur de colonnes utilisé dans le CSV généré|
|  `accountgenerator.view.preview.size` | 20| Nombre de comptes générés affichés dans la table d'aperçu sur la page de détail du job|
|  `accountgenerator.generation.certifier.*` | fccertifier| Code du certificateur pour chaque attribut d'identité (first_name, family-name, birthplace_code, birthcountry_code, gender, birthdate, email, login)|
|  `accountgenerator.generation.value.birthcountry_code` | 99100| Code pays de naissance (99100 = France)|
|  `accountgenerator.generation.value.max.birthdate.year` | 2000| Année maximale pour la génération aléatoire de la date de naissance|
|  `accountgenerator.generation.value.max.birthdate.month` | 12| Mois maximum pour la génération aléatoire de la date de naissance|
|  `accountgenerator.generation.value.max.birthdate.day` | 31| Jour maximum pour la génération aléatoire de la date de naissance|

 **Beans Spring** 

Le contexte Spring est défini dans `accountgenerator_context.xml` . Les beans suivants sont déclarés :

| ID du bean| Classe| Description|
|-----------------|-----------------|-----------------|
|  `accountgenerator.identityService` | IdentityService| Service client pour l'API IdentityStore (utilise HttpApiManagerAccessTransport)|
|  `accountgenerator.accountManagementService` | AccountManagementService| Service client pour l'API AccountManagement|
|  `accountgenerator.geocodesCache` | GeocodesCache| Service de cache pour les codes communes de l'API Geocodes|
|  `accountgenerator.accountDao` | IdentityAccountDao| DAO pour la table accountgenerator_account|
|  `accountgenerator.jobDao` | AccountGenerationJobDao| DAO pour la table accountgenerator_job|
|  `accountgenerator.jobExecutor` | java.util.concurrent.ExecutorService| Pool de threads fixe exécutant les jobs en arrière-plan (dimensionné par accountgenerator.executor.threads)|
|  `accountgenerator.jobService` | AccountGenerationJobService| Orchestrateur des jobs : valide la requête, persiste le job, le planifie sur l'exécuteur, suit la progression, écrit le CSV, et publie l'URL de téléchargement.|
|  `accountgenerator.localFileSystemDirectoryFileService` | LocalFileSystemDirectoryFileService| Implémentation d'IFileStoreServiceProvider qui stocke les CSV générés sur disque dans `accountgenerator.filesystem.path`. Enregistrée auprès du FileService de Lutece sous le même nom.|
|  `accountgenerator.defaultFileDownloadUrlService` | DefaultFileDownloadService| Service Lutece core produisant les URLs signées de téléchargement back-office|
|  `accountgenerator.defaultFileNoRBACService` | DefaultFileNoRBACService| Stratégie RBAC permissive : tout administrateur authentifié peut télécharger le fichier|

 **Daemons** 

Le plugin déclare un daemon :

| ID du daemon| Classe| Description|
|-----------------|-----------------|-----------------|
|  `purgeExpiratedIdentityAccountsDaemons` | PurgeExpiratedIdentityAccountsDaemons| Purge périodiquement les comptes générés expirés ainsi que les identités associées en appelant les API IdentityStore et AccountManagement.|

 **Caches** 

| Nom du cache| Classe| Description|
|-----------------|-----------------|-----------------|
|  `AccountGeneratorGeocodesCache` | GeocodesCache| Met en cache les codes communes récupérés depuis l'API Geocodes, indexés par date de référence. Évite les appels HTTP répétés pour la génération des codes de lieu de naissance.|

 **Base de données** 

Les scripts SQL sont dans `src/sql/plugins/accountgenerator/`. Deux tables sont créées :

| Table| Description|
|-----------------|-----------------|
|  `accountgenerator_account` | Suivi de chaque identité / compte MonParis générés avec leurs dates de création et d'expiration, pour purge ultérieure. Chaque ligne porte la `job_reference` du job qui l'a créée, ce qui permet à un job de cibler la suppression de ses propres comptes.|
|  `accountgenerator_job` | Enregistre chaque job asynchrone : référence (UUID, unique), statut (PENDING / IN_PROGRESS / COMPLETED / FAILED), payload de la requête, compteurs (traités, succès, échecs), clé du fichier CSV, message d'erreur éventuel, et `date_accounts_deletion` une fois que l'action "Supprimer les comptes générés" a été déclenchée.|

## Usage

 **Droits d'administration** 

| Clé du droit| Description| URL d'administration|
|-----------------|-----------------|-----------------|
|  `ACCOUNTGENERATOR_MANAGEMENT` | Droit d'accès à la fonctionnalité de génération de comptes en Back Office| jsp/admin/plugins/accountgenerator/AccountGeneratorManagement.jsp|

 **Vues back office** 

| Vue| Description|
|-----------------|-----------------|
|  `manageJobs` (vue par défaut)| Liste des jobs avec leur référence, date de création, utilisateur, statut et avancement. Bouton "Nouveau job".|
|  `createJob` | Formulaire de soumission d'un nouveau job. Les champs du formulaire sont mémorisés en cas d'erreur de validation.|
|  `viewJob` | Page de détail d'un job : métadonnées, barre de progression en temps réel (via ProgressManagerService) pendant l'exécution, lien de téléchargement du CSV une fois terminé, aperçu tabulaire des `accountgenerator.view.preview.size` premiers comptes générés (email / mot de passe / CUID / GUID), et bouton "Supprimer les comptes générés" qui efface l'ensemble des identités externes, comptes MonParis et lignes locales créés par ce job.|

 **Services** 

| Service| Méthode| Description|
|-----------------|-----------------|-----------------|
|  `AccountGenerationJobService` |  `submit(AccountGenerationDto, clientCode, appCode, authorName, authorType, user)` | Valide la requête, persiste un job PENDING, le planifie sur l'exécuteur et retourne le job (avec sa référence). Lève `RequestFormatException` si la requête est invalide (aucune ligne insérée dans ce cas).|
|  `AccountGenerationJobService` |  `findByReference(String) / findAll() / getProgressFeedToken(String) / getDownloadUrl(AccountGenerationJob)` | Helpers de consultation utilisés par le back office et l'endpoint REST de statut. La liste des jobs est triée du plus récent au plus ancien (`date_creation DESC, id_job DESC`).|
|  `AccountGenerationJobService` |  `getCsvPreview(AccountGenerationJob, int)` | Retourne les *N* premières lignes du CSV du job (email, mot de passe, CUID, GUID) sans charger tout le fichier. Vide une fois les comptes supprimés.|
|  `AccountGenerationJobService` |  `deleteGeneratedAccounts(String reference)` | Suppression idempotente et ciblée : supprime tous les comptes tagués avec la référence de ce job dans les API externes et la base locale, supprime le CSV sur disque, et positionne `date_accounts_deletion` sur le job.|
|  `AccountGenerationJobValidationService` |  `validate(AccountGenerationDto)` | Validation stricte (taille de lot dans les bornes, validité positive, birthdate parseable en dd/MM/yyyy, loginPrefix/loginSuffix cohérents).|
|  `IdentityAccountGeneratorService` |  `createIdentityAccountBatch(AccountGenerationDto, String jobReference, GeneratedAccountConsumer)` | Génère le lot et streame chaque compte généré vers le consumer fourni (utilisé par le service de job pour écrire le CSV et avancer le feed de progression), en persistant les identity-accounts par blocs. Chaque ligne persistée est taguée avec la `jobReference` fournie.|
|  `IdentityAccountPurgeService` |  `purge()` | Charge les comptes expirés depuis la base de données et supprime les comptes MonParis et identités associés via les API externes. Retourne des statistiques de purge.|
|  `IdentityAccountPurgeService` |  `purgeByJobReference(String)` | Appelé par `deleteGeneratedAccounts`. Charge toutes les lignes `accountgenerator_account` portant la référence, les supprime dans AccountManagement + IdentityStore, puis les retire de la table locale.|

 **API REST** 

| Verbe HTTP| Chemin| Description|
|-----------------|-----------------|-----------------|
| POST|  `/rest/identitystore/api/v3/generator/account-generator` | Soumettre un job de génération asynchrone. Retourne `202 Accepted` avec une référence, ou `400 Bad Request` en cas de validation en échec (dans ce cas aucun job n'est créé).|
| GET|  `/rest/identitystore/api/v3/generator/account-generator/job/{reference}` | Consulter le statut courant d'un job : compteurs, pourcentage de progression, rapport (pendant l'exécution) et URL de téléchargement (une fois terminé).|

 **En-têtes de la requête :** 

*  `X-Client-Code` - code de l'application cliente
*  `X-Author-Name` - nom de l'auteur
*  `X-Author-Type` - type de l'auteur
*  `X-Application-Code` - code de l'application

 **Corps de la requête POST (JSON) :** 

| Champ| Type| Description|
|-----------------|-----------------|-----------------|
|  `generation.generateAccount` | boolean| Créer également un compte MonParis pour chaque identité|
|  `generation.generationIncrementOffset` | integer| Offset ajouté au compteur d'itération pour la génération des attributs|
|  `generation.nbDaysOfValidity` | integer| Nombre de jours avant l'expiration des comptes générés|
|  `generation.batchSize` | integer| Nombre d'identités/comptes à générer (plafonné par `generation.limit`)|
|  `generation.loginPrefix` / `generation.loginSuffix` | string| Format de l'email : `[loginPrefix][offset+i][loginSuffix]`. Les deux doivent être fournis ensemble.|
|  `generation.password` | string| Mot de passe commun. Fallback : `accountgenerator.generation.password` si vide.|
|  `generation.firstNamePrefix` / `generation.familyNamePrefix` | string| Préfixes facultatifs pour les noms/prénoms randomisés.|
|  `generation.birthdate` | string (dd/MM/yyyy)| Date de naissance fixée. Si absente, une date aléatoire est générée.|
|  `generation.birthCountryCode` / `generation.birthplaceCode` | string| Codes géographiques fixés. Sinon le code commune est pris dans le cache Geocodes.|
|  `generation.identityCertifier` / `generation.mailLoginCertifier` | string| Certificateurs appliqués aux attributs (surcharge les défauts des properties).|

 **Corps de la réponse POST (JSON) :** une enveloppe de job avec la référence fraîchement créée et son statut initial.

```json
{
  "reference": "9b1f8c02-7a62-4c85-b3a3-dbb0d9f8b1a1",
  "status": "PENDING",
  "creation_date": "2026-04-21T17:12:33Z",
  "batch_size": 1000
}
```

 **Corps de la réponse GET (JSON) :** état courant du job avec pourcentage d'avancement, rapport de progression (pendant l'exécution) et URL de téléchargement (une fois terminé).

```json
{
  "reference": "9b1f8c02-7a62-4c85-b3a3-dbb0d9f8b1a1",
  "status": "COMPLETED",
  "creation_date": "2026-04-21T17:12:33Z",
  "completion_date": "2026-04-21T17:18:07Z",
  "batch_size": 1000,
  "nb_processed": 1000,
  "nb_success": 998,
  "nb_failure": 2,
  "progress_percent": 100,
  "download_url": "jsp/admin/file/download?..."
}
```

 **Sortie CSV :** une fois terminé, le job produit un fichier nommé `accountgenerator-<reference>.csv` dans `accountgenerator.filesystem.path`. Colonnes : `email`, `password`, `cuid`, `guid`, séparées par `accountgenerator.generation.csv.separator`.


[Maven documentation and reports](https://dev.lutece.paris.fr/plugins/plugin-accountgenerator/)



 *generated by [xdoc2md](https://github.com/lutece-platform/tools-maven-xdoc2md-plugin) - do not edit directly.*
