CREATE TABLE delivery_bank_history.revinfo
(
    rev BIGSERIAL PRIMARY KEY,
    revtmstmp BIGINT
);

CREATE TABLE delivery_bank_history.countries_history
(
    id INTEGER NOT NULL,
    rev BIGINT NOT NULL,
    revtype SMALLINT NOT NULL,
    deleted  boolean NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    deleted_by VARCHAR(255),
    name VARCHAR(128) NOT NULL,
    code VARCHAR(3) NOT NULL,

    CONSTRAINT pk_countries_history PRIMARY KEY (id, rev),
    CONSTRAINT fk_countries_history_rev FOREIGN KEY (rev) REFERENCES delivery_bank_history.revinfo (rev)
);

CREATE INDEX IF NOT EXISTS idx_countries_history_revision ON delivery_bank_history.countries_history (rev);

CREATE TABLE delivery_bank_history.addresses_history
(
    id VARCHAR NOT NULL,
    rev BIGINT NOT NULL,
    revtype SMALLINT NOT NULL,
    deleted boolean NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    deleted_by VARCHAR(128),
    country_id INTEGER NOT NULL,
    address VARCHAR(128) NOT NULL,
    zip_code VARCHAR(32) NOT NULL,
    city VARCHAR(128) NOT NULL,

    CONSTRAINT pk_addresses_history PRIMARY KEY (id, rev),
    CONSTRAINT fk_addresses_history_rev FOREIGN KEY (rev) REFERENCES delivery_bank_history.revinfo (rev)
);

CREATE INDEX IF NOT EXISTS idx_addresses_history_revision ON delivery_bank_history.addresses_history (rev);

CREATE TABLE delivery_bank_history.recipients_history
(
    id VARCHAR NOT NULL,
    rev BIGINT NOT NULL,
    revtype SMALLINT NOT NULL,
    deleted boolean NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    deleted_by VARCHAR(255),
    user_id VARCHAR NOT NULL,
    first_name VARCHAR(64) NOT NULL,
    last_name VARCHAR(64) NOT NULL,
    middle_name VARCHAR(64),
    contact_phone VARCHAR(32) NOT NULL,
    office_id VARCHAR,
    person_type VARCHAR(20) NOT NULL,

    CONSTRAINT pk_recipients_history PRIMARY KEY (id, rev),
    CONSTRAINT fk_recipients_history_rev FOREIGN KEY (rev) REFERENCES delivery_bank_history.revinfo (rev)
);

CREATE INDEX IF NOT EXISTS idx_recipients_history_revision ON delivery_bank_history.recipients_history (rev);

CREATE TABLE delivery_bank_history.deliveries_history
(
    id VARCHAR NOT NULL,
    rev BIGINT NOT NULL,
    revtype SMALLINT NOT NULL,
    deleted boolean NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    deleted_by VARCHAR(255),
    order_id VARCHAR NOT NULL,
    recipient_id VARCHAR NOT NULL REFERENCES delivery_bank.recipients (id),
    courier_id VARCHAR,
    courier_contact_phone VARCHAR(32),
    origin_address_id VARCHAR NOT NULL REFERENCES delivery_bank.addresses (id),
    destination_address_id VARCHAR NOT NULL REFERENCES delivery_bank.addresses (id),
    status VARCHAR(20) NOT NULL,
    delivery_duration INTERVAL,
    pickup_date TIMESTAMP WITHOUT TIME ZONE,
    delivered_at TIMESTAMP WITHOUT TIME ZONE,

    CONSTRAINT pk_deliveries_history PRIMARY KEY (id, rev),
    CONSTRAINT fk_deliveries_history_rev FOREIGN KEY (rev) REFERENCES delivery_bank_history.revinfo (rev)
);

CREATE INDEX IF NOT EXISTS idx_deliveries_history_revision ON delivery_bank_history.deliveries_history (rev);