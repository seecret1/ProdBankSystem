CREATE TABLE bank_history.revinfo
(
    rev BIGSERIAL PRIMARY KEY,
    revtmstmp BIGINT
);

CREATE TABLE bank_history.countries_history
(
    id INTEGER NOT NULL,
    revision BIGINT NOT NULL,
    revision_type SMALLINT NOT NULL,
    deleted  boolean NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_by VARCHAR(128),
    name VARCHAR(128) NOT NULL,
    code VARCHAR(3) NOT NULL,

    CONSTRAINT pk_counties_history PRIMARY KEY (id, revision),
    CONSTRAINT fk_countries_history_rev FOREIGN KEY (revision) REFERENCES bank_history.revinfo (rev)
);

CREATE INDEX IF NOT EXISTS idx_countries_history_revision ON bank_history.countries_history (revision);

CREATE TABLE bank_history.addresses_history
(
    id UUID NOT NULL,
    revision BIGINT NOT NULL,
    revision_type SMALLINT NOT NULL,
    deleted boolean NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_by VARCHAR(128),
    country_id INTEGER NOT NULL,
    address VARCHAR(128) NOT NULL,
    zip_code VARCHAR(32) NOT NULL,
    city VARCHAR(128) NOT NULL,

    CONSTRAINT pk_addresses_history PRIMARY KEY (id, revision),
    CONSTRAINT fk_addresses_history_rev FOREIGN KEY (revision) REFERENCES bank_history.revinfo (rev)
);

CREATE INDEX IF NOT EXISTS idx_addresses_history_revision ON bank_history.addresses_history (revision);


CREATE TABLE bank_history.users_history
(
    id UUID NOT NULL,
    revision BIGINT NOT NULL,
    revision_type SMALLINT NOT NULL,
    status VARCHAR(15) NOT NULL,
    deleted boolean NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_by VARCHAR(255),
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(64) NOT NULL,
    last_name VARCHAR(64) NOT NULL,
    middle_name VARCHAR(64),
    birth_date VARCHAR(64) NOT NULL,
    role VARCHAR(15) NOT NULL,
    address_id UUID NOT NULL,

    CONSTRAINT pk_users_history PRIMARY KEY (id, revision),
    CONSTRAINT fk_users_history_rev FOREIGN KEY (revision) REFERENCES bank_history.revinfo (rev)
);

CREATE INDEX IF NOT EXISTS idx_users_history_revision ON bank_history.users_history (revision);

CREATE TABLE bank_history.individuals_history
(
    id UUID NOT NULL,
    revision BIGINT NOT NULL,
    revision_type SMALLINT NOT NULL,
    deleted boolean NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_by VARCHAR(255),
    passport_number VARCHAR(64) NOT NULL,
    phone_number VARCHAR(64) NOT NULL,
    user_id UUID NOT NULL,

    CONSTRAINT pk_individuals_history PRIMARY KEY (id, revision),
    CONSTRAINT fk_individuals_history_rev FOREIGN KEY (revision) REFERENCES bank_history.revinfo (rev)
);

CREATE INDEX IF NOT EXISTS idx_individuals_history_revision ON bank_history.individuals_history (revision);