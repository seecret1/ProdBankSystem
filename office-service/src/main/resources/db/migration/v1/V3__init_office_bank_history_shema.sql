CREATE TABLE office_bank_history.revinfo
(
    rev BIGSERIAL PRIMARY KEY,
    revtmstmp BIGINT
);

CREATE TABLE office_bank_history.countries_history
(
    id INTEGER NOT NULL,
    rev BIGINT NOT NULL,
    revtype SMALLINT NOT NULL,
    deleted  boolean NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_by VARCHAR(128),
    name VARCHAR(128) NOT NULL,
    code VARCHAR(3) NOT NULL,

    CONSTRAINT pk_counties_history PRIMARY KEY (id, rev),
    CONSTRAINT fk_countries_history_rev FOREIGN KEY (rev) REFERENCES office_bank_history.revinfo (rev)
);

CREATE INDEX IF NOT EXISTS idx_countries_history_revision ON office_bank_history.countries_history (rev);

CREATE TABLE office_bank_history.addresses_history
(
    id VARCHAR NOT NULL,
    rev BIGINT NOT NULL,
    revtype SMALLINT NOT NULL,
    deleted boolean NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_by VARCHAR(128),
    country_id INTEGER NOT NULL,
    address VARCHAR(128) NOT NULL,
    zip_code VARCHAR(32) NOT NULL,
    city VARCHAR(128) NOT NULL,

    CONSTRAINT pk_addresses_history PRIMARY KEY (id, rev),
    CONSTRAINT fk_addresses_history_rev FOREIGN KEY (rev) REFERENCES office_bank_history.revinfo (rev)
);

CREATE INDEX IF NOT EXISTS idx_addresses_history_revision ON office_bank_history.addresses_history (rev);

CREATE TABLE office_bank_history.offices_history
(
    id VARCHAR NOT NULL,
    rev BIGINT NOT NULL,
    revtype SMALLINT NOT NULL,
    deleted boolean NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_by VARCHAR(255),
    name VARCHAR(64) NOT NULL,
    contact_phone VARCHAR(64) NOT NULL,
    schedule_json VARCHAR(1024) NOT NULL,
    active boolean NOT NULL DEFAULT TRUE,
    address_id VARCHAR NOT NULL REFERENCES office_bank.addresses (id)

    CONSTRAINT pk_offices_history PRIMARY KEY (id, rev),
    CONSTRAINT fk_offices_history_rev FOREIGN KEY (rev) REFERENCES office_bank_history.revinfo (rev)
);

CREATE INDEX IF NOT EXISTS idx_offices_history_revision ON office_bank_history.offices_history (rev);