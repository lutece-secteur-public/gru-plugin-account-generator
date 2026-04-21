--
-- Structure for table accountgenerator_account
--
DROP TABLE IF EXISTS accountgenerator_account;
CREATE TABLE accountgenerator_account (
    guid VARCHAR(255),
    cuid VARCHAR(255),
    creationDate DATE NOT NULL,
    expirationDate DATE NOT NULL,
    job_reference VARCHAR(64),
    KEY idx_accountgenerator_account_job_reference (job_reference)
);

--
-- Structure for table accountgenerator_job (asynchronous generation jobs)
--
DROP TABLE IF EXISTS accountgenerator_job;
CREATE TABLE accountgenerator_job (
    id_job          INT AUTO_INCREMENT,
    reference       VARCHAR(64) NOT NULL,
    status          VARCHAR(32) NOT NULL,
    date_creation   TIMESTAMP NOT NULL,
    date_completion TIMESTAMP NULL,
    date_accounts_deletion TIMESTAMP NULL,
    user_name       VARCHAR(255),
    client_code     VARCHAR(128),
    app_code        VARCHAR(128),
    author_name     VARCHAR(255),
    author_type     VARCHAR(64),
    batch_size      INT NOT NULL DEFAULT 0,
    nb_processed    INT NOT NULL DEFAULT 0,
    nb_success      INT NOT NULL DEFAULT 0,
    nb_failure      INT NOT NULL DEFAULT 0,
    request_json    LONGTEXT,
    file_key        VARCHAR(255),
    file_name       VARCHAR(255),
    error_message   LONGTEXT,
    PRIMARY KEY (id_job),
    UNIQUE KEY uk_accountgenerator_job_reference (reference)
);
