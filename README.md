![](https://dev.lutece.paris.fr/jenkins/buildStatus/icon?job=gru-plugin-account-generator-deploy)
[![Alerte](https://dev.lutece.paris.fr/sonar/api/project_badges/measure?project=fr.paris.lutece.plugins%3Aplugin-accountgenerator&metric=alert_status)](https://dev.lutece.paris.fr/sonar/dashboard?id=fr.paris.lutece.plugins%3Aplugin-accountgenerator)
[![Line of code](https://dev.lutece.paris.fr/sonar/api/project_badges/measure?project=fr.paris.lutece.plugins%3Aplugin-accountgenerator&metric=ncloc)](https://dev.lutece.paris.fr/sonar/dashboard?id=fr.paris.lutece.plugins%3Aplugin-accountgenerator)
[![Coverage](https://dev.lutece.paris.fr/sonar/api/project_badges/measure?project=fr.paris.lutece.plugins%3Aplugin-accountgenerator&metric=coverage)](https://dev.lutece.paris.fr/sonar/dashboard?id=fr.paris.lutece.plugins%3Aplugin-accountgenerator)

# Plugin accountgenerator

## Introduction

The `plugin-accountgenerator` plugin provides automated batch creation and lifecycle management of user identities and MonParis accounts. It is designed for test and pre-production environments to quickly generate realistic user data with certified attributes (name, birthdate, gender, email, birthplace, etc.).

Each generation request is persisted as a **job** with a reference token (UUID). Jobs are processed asynchronously by a background executor so that large batches (up to 100 000 accounts) do not block the caller. Job progress is exposed through the Lutece `ProgressManagerService` and via a consultation REST endpoint. Once a job completes, the generated data (email, password, CUID, GUID) is available as a downloadable CSV file.

The plugin offers two ways to submit a job:

* A **REST API**: `POST` to submit a job, `GET` to poll its status by reference.
* A **Back Office admin feature** with three views (jobs list, submission form, job detail with progress bar and download link).

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
|  `accountgenerator.identitystore.ApiEndPointUrl` | | IdentityStore API base URL|
|  `accountgenerator.identitystore.accessManagerEndPointUrl` | | Access manager endpoint URL for IdentityStore API calls|
|  `accountgenerator.identitystore.accessManagerCredentials` | | Access manager credentials for IdentityStore API calls|
|  `accountgenerator.accountManagement.ApiEndPointUrl` | | AccountManagement API base URL|
|  `accountgenerator.accountManagement.path` | | AccountManagement API path|
|  `accountgenerator.accountManagement.client-code` | | Client code for IdentityStore API calls|
|  `accountgenerator.accountManagement.client-name` | | Client name for IdentityStore API calls|
|  `accountgenerator.accountManagement.client-id` | | Client ID for AccountManagement API|
|  `accountgenerator.accountManagement.secret-id` | | Client secret for AccountManagement API|
|  `accountgenerator.geocodes.city.codes.endpoint` | | Geocodes API endpoint for city codes|
|  `accountgenerator.generation.limit` | 100000| Maximum batch size accepted by a single job submission|
|  `accountgenerator.generation.password` | password123456789| Default password assigned to all generated accounts when none is provided in the request|
|  `accountgenerator.generation.mail.suffix` | @paris.test.fr| Email suffix for generated accounts (legacy pattern)|
|  `accountgenerator.executor.threads` | 2| Size of the fixed thread pool running generation jobs asynchronously|
|  `accountgenerator.filesystem.path` | /tmp/accountgenerator| Directory where generated CSV files are written. Must be writable by the application server.|
|  `accountgenerator.generation.file.flush.size` | 10| Number of generated accounts between two CSV flushes and two progress report lines|
|  `accountgenerator.generation.db.flush.size` | 500| Number of storable identity-accounts buffered before a DB insert|
|  `accountgenerator.generation.csv.separator` | ;| Column separator used in the generated CSV file|
|  `accountgenerator.view.preview.size` | 20| Number of generated accounts shown in the preview table on the job detail page|
|  `accountgenerator.generation.certifier.*` | fccertifier| Certifier code for each identity attribute (first_name, family-name, birthplace_code, birthcountry_code, gender, birthdate, email, login)|
|  `accountgenerator.generation.value.birthcountry_code` | 99100| Birth country code (99100 = France)|
|  `accountgenerator.generation.value.max.birthdate.year` | 2000| Maximum year for random birthdate generation|
|  `accountgenerator.generation.value.max.birthdate.month` | 12| Maximum month for random birthdate generation|
|  `accountgenerator.generation.value.max.birthdate.day` | 31| Maximum day for random birthdate generation|

 **Spring Beans** 

The Spring context is defined in `accountgenerator_context.xml` . The following beans are declared:

| Bean ID| Class| Description|
|-----------------|-----------------|-----------------|
|  `accountgenerator.identityService` | IdentityService| Client service for the IdentityStore API (wired with HttpApiManagerAccessTransport)|
|  `accountgenerator.accountManagementService` | AccountManagementService| Client service for the AccountManagement API|
|  `accountgenerator.geocodesCache` | GeocodesCache| Cache service for city codes from the Geocodes API|
|  `accountgenerator.accountDao` | IdentityAccountDao| DAO for the accountgenerator_account table|
|  `accountgenerator.jobDao` | AccountGenerationJobDao| DAO for the accountgenerator_job table|
|  `accountgenerator.jobExecutor` | java.util.concurrent.ExecutorService| Fixed thread pool that runs generation jobs in background (sized by accountgenerator.executor.threads)|
|  `accountgenerator.jobService` | AccountGenerationJobService| Core job orchestrator: validates input, persists the job, schedules it on the executor, tracks progress, writes the CSV, and publishes the download URL.|
|  `accountgenerator.localFileSystemDirectoryFileService` | LocalFileSystemDirectoryFileService| IFileStoreServiceProvider implementation that stores generated CSV files on disk at `accountgenerator.filesystem.path`. Registered with the Lutece FileService under the same name.|
|  `accountgenerator.defaultFileDownloadUrlService` | DefaultFileDownloadService| Lutece core service producing signed back-office download URLs for generated files|
|  `accountgenerator.defaultFileNoRBACService` | DefaultFileNoRBACService| Permissive RBAC strategy: any authenticated admin can download the file|

 **Daemons** 

The plugin declares one daemon:

| Daemon ID| Class| Description|
|-----------------|-----------------|-----------------|
|  `purgeExpiratedIdentityAccountsDaemons` | PurgeExpiratedIdentityAccountsDaemons| Periodically purges expired generated accounts and their associated identities by calling the IdentityStore and AccountManagement APIs.|

 **Caches** 

| Cache name| Class| Description|
|-----------------|-----------------|-----------------|
|  `AccountGeneratorGeocodesCache` | GeocodesCache| Caches city codes fetched from the Geocodes API, keyed by date reference. Avoids repeated HTTP calls for birthplace code generation.|

 **Database** 

SQL scripts are located in `src/sql/plugins/accountgenerator/`. Two tables are created:

| Table| Description|
|-----------------|-----------------|
|  `accountgenerator_account` | Tracks each generated identity / MonParis account with its creation and expiration dates for later purge. Each row carries the `job_reference` of the job that created it so that a job can scope-delete its own accounts.|
|  `accountgenerator_job` | Records each asynchronous generation job: reference (UUID, unique), status (PENDING / IN_PROGRESS / COMPLETED / FAILED), request payload, counters (processed, success, failure), CSV file key, optional error message, and `date_accounts_deletion` once the "Delete generated accounts" action has been triggered.|

## Usage

 **Admin Rights** 

| Right key| Description| Admin URL|
|-----------------|-----------------|-----------------|
|  `ACCOUNTGENERATOR_MANAGEMENT` | Right to access the account generation back office feature| jsp/admin/plugins/accountgenerator/AccountGeneratorManagement.jsp|

 **Back office views** 

| View| Description|
|-----------------|-----------------|
|  `manageJobs` (default)| List of all jobs with their reference, creation date, user, status and progress. Offers a "New job" button.|
|  `createJob` | Form to submit a new generation job. Form fields are remembered across validation errors.|
|  `viewJob` | Job detail page showing metadata, a live progress bar (via ProgressManagerService) while running, a download link to the generated CSV once completed, a preview table of the first `accountgenerator.view.preview.size` generated accounts (email / password / CUID / GUID), and a "Delete generated accounts" button that wipes all external identities, MonParis accounts and local DB rows created by this job.|

 **Services** 

| Service| Method| Description|
|-----------------|-----------------|-----------------|
|  `AccountGenerationJobService` |  `submit(AccountGenerationDto, clientCode, appCode, authorName, authorType, user)` | Validates input, persists a PENDING job, schedules it on the executor and returns the job (with its reference). Throws `RequestFormatException` on invalid input, in which case no row is inserted.|
|  `AccountGenerationJobService` |  `findByReference(String) / findAll() / getProgressFeedToken(String) / getDownloadUrl(AccountGenerationJob)` | Job consultation helpers used by the back office and the status REST endpoint. The job list is sorted most-recent-first (`date_creation DESC, id_job DESC`).|
|  `AccountGenerationJobService` |  `getCsvPreview(AccountGenerationJob, int)` | Returns the first *N* rows of the job's CSV file (email, password, CUID, GUID) without loading the whole file. Empty once the accounts have been deleted.|
|  `AccountGenerationJobService` |  `deleteGeneratedAccounts(String reference)` | Idempotent scope-delete: removes every account tagged with this job's reference from the external APIs and the local DB, deletes the CSV from disk, and stamps `date_accounts_deletion` on the job.|
|  `AccountGenerationJobValidationService` |  `validate(AccountGenerationDto)` | Strict parameter validation (batch size within bounds, positive validity, parseable dd/MM/yyyy birthdate, consistent loginPrefix/loginSuffix).|
|  `IdentityAccountGeneratorService` |  `createIdentityAccountBatch(AccountGenerationDto, String jobReference, GeneratedAccountConsumer)` | Generates the batch and streams each generated account to the given consumer (used by the job service to write the CSV and advance the progress feed), persisting storable identity-accounts in chunks. Each persisted row is tagged with the supplied `jobReference`.|
|  `IdentityAccountPurgeService` |  `purge()` | Loads expired accounts from the database and deletes associated MonParis accounts and identities via external APIs. Returns purge statistics.|
|  `IdentityAccountPurgeService` |  `purgeByJobReference(String)` | Called by `deleteGeneratedAccounts`. Loads all `accountgenerator_account` rows with the given reference, deletes them from AccountManagement + IdentityStore, then removes them from the local table.|

 **REST API** 

| HTTP verb| Path| Description|
|-----------------|-----------------|-----------------|
| POST|  `/rest/identitystore/api/v3/generator/account-generator` | Submit an asynchronous generation job. Returns `202 Accepted` with a reference token, or `400 Bad Request` on validation error (in which case no job is created).|
| GET|  `/rest/identitystore/api/v3/generator/account-generator/job/{reference}` | Get a job's current status, counters, progress percentage, progress report (while running) and download URL (once completed).|

 **Request headers:** 

*  `X-Client-Code` - client application code
*  `X-Author-Name` - author name
*  `X-Author-Type` - author type
*  `X-Application-Code` - application code

 **POST request body (JSON):** 

| Field| Type| Description|
|-----------------|-----------------|-----------------|
|  `generation.generateAccount` | boolean| Whether to also create a MonParis account for each identity|
|  `generation.generationIncrementOffset` | integer| Offset added to the iteration counter for attribute generation|
|  `generation.nbDaysOfValidity` | integer| Number of days before the generated accounts expire|
|  `generation.batchSize` | integer| Number of identities/accounts to generate (capped by `generation.limit`)|
|  `generation.loginPrefix` / `generation.loginSuffix` | string| Email format: `[loginPrefix][offset+i][loginSuffix]`. Must both be provided together.|
|  `generation.password` | string| Common password to assign. Falls back to `accountgenerator.generation.password` if empty.|
|  `generation.firstNamePrefix` / `generation.familyNamePrefix` | string| Optional prefixes for randomized first/family names.|
|  `generation.birthdate` | string (dd/MM/yyyy)| Fixed birthdate. If omitted, a random date is generated.|
|  `generation.birthCountryCode` / `generation.birthplaceCode` | string| Fixed geography codes. If omitted, the birthplace is picked from the Geocodes cache.|
|  `generation.identityCertifier` / `generation.mailLoginCertifier` | string| Certifiers applied to the generated attributes (overrides the property defaults).|

 **POST response body (JSON):** A job envelope with the newly created reference and its initial status.

```json
{
  "reference": "9b1f8c02-7a62-4c85-b3a3-dbb0d9f8b1a1",
  "status": "PENDING",
  "creation_date": "2026-04-21T17:12:33Z",
  "batch_size": 1000
}
```

 **GET response body (JSON):** Current job state including progress percentage, the latest progress report lines (while running) and the download URL once the job has completed.

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

 **CSV output:** Once completed, the job produces a file named `accountgenerator-<reference>.csv` in `accountgenerator.filesystem.path`. Columns: `email`, `password`, `cuid`, `guid`, separated by `accountgenerator.generation.csv.separator`.


[Maven documentation and reports](https://dev.lutece.paris.fr/plugins/plugin-accountgenerator/)



 *generated by [xdoc2md](https://github.com/lutece-platform/tools-maven-xdoc2md-plugin) - do not edit directly.*
