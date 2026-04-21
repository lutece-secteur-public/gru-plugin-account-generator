DROP TABLE IF EXISTS accountgenerator_account;
CREATE TABLE accountgenerator_account (
    guid VARCHAR(255),
    cuid VARCHAR(255),
    creationDate DATE NOT NULL,
    expirationDate DATE NOT NULL
);
