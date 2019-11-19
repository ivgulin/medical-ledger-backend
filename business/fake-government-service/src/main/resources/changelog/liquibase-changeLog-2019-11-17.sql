--liquibase formatted sql
--changeset gulini:2

DROP TABLE known_identity CASCADE;

CREATE TABLE IF NOT EXISTS diploma
(
    id              SERIAL       NOT NULL
        CONSTRAINT diploma_pkey
            PRIMARY KEY,
    number          VARCHAR(255) NOT NULL,
    first_name      VARCHAR(255) NOT NULL,
    last_name       VARCHAR(255) NOT NULL,
    father_name     VARCHAR(255) NOT NULL,
    place_of_study  VARCHAR(255) NOT NULL,
    course_of_study VARCHAR(255) NOT NULL,
    date_of_issue   BIGINT       NOT NULL,
    qualification   VARCHAR(255) NOT NULL,
    issuer          VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS address
(
    id                  SERIAL       NOT NULL
        CONSTRAINT address_pkey
            PRIMARY KEY,
    administrative_area VARCHAR(255) NOT NULL,
    street              VARCHAR(255) NOT NULL,
    street_number       VARCHAR(255) NOT NULL,
    apartment_number    VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS national_passport
(
    id             SERIAL       NOT NULL
        CONSTRAINT national_passport_pkey
            PRIMARY KEY,
    number         VARCHAR(255) NOT NULL,
    first_name     VARCHAR(255) NOT NULL,
    last_name      VARCHAR(255) NOT NULL,
    father_name    VARCHAR(255) NOT NULL,
    date_of_birth  BIGINT       NOT NULL,
    place_of_birth VARCHAR(255) NOT NULL,
    image_path     VARCHAR(255),
    sex            VARCHAR(255) NOT NULL,
    issuer         VARCHAR(255) NOT NULL,
    date_of_issue  BIGINT       NOT NULL
);

CREATE TABLE IF NOT EXISTS place_of_residence
(
    id                   SERIAL  NOT NULL
        CONSTRAINT place_of_residence_pkey
            PRIMARY KEY,
    address_id           INTEGER NOT NULL
        CONSTRAINT fk_por_address
            REFERENCES address,
    start_date           BIGINT  NOT NULL,
    end_date             BIGINT,
    national_passport_id INTEGER
        CONSTRAINT fk_por_national_passport
            REFERENCES national_passport
);

CREATE TABLE IF NOT EXISTS national_number
(
    number            VARCHAR(255) NOT NULL UNIQUE
        CONSTRAINT national_number_pkey
            PRIMARY KEY,
    registration_date BIGINT       NOT NULL,
    issuer            VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS known_identity
(
    id                   SERIAL       NOT NULL
        CONSTRAINT known_identity_pkey
            PRIMARY KEY,
    role                 VARCHAR(255) NOT NULL,
    national_passport_id INTEGER      NOT NULL
        CONSTRAINT fk_ki_national_passport
            REFERENCES national_passport,
    national_number      VARCHAR(255) NOT NULL
        CONSTRAINT fk_ki_national_number
            REFERENCES national_number,
    diploma_id           INTEGER
        CONSTRAINT fk_ki_diploma
            REFERENCES diploma
);

CREATE TABLE IF NOT EXISTS certificate
(
    id                SERIAL       NOT NULL
        CONSTRAINT certificate_pkey
            PRIMARY KEY,
    number            VARCHAR(255) NOT NULL,
    first_name        VARCHAR(255) NOT NULL,
    last_name         VARCHAR(255) NOT NULL,
    father_name       VARCHAR(255) NOT NULL,
    course_of_study   VARCHAR(255) NOT NULL,
    category          VARCHAR(255) NOT NULL,
    qualification     VARCHAR(255) NOT NULL,
    date_of_exam      BIGINT       NOT NULL,
    date_of_issue     BIGINT       NOT NULL,
    expires_in        BIGINT       NOT NULL,
    issuer            VARCHAR(255) NOT NULL,
    known_identity_id INTEGER
        CONSTRAINT fk_c_known_identity
            REFERENCES known_identity
);