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

CREATE TABLE delivery_bank_history.couriers_history
(
    id VARCHAR NOT NULL,
    rev BIGINT NOT NULL,
    revtype SMALLINT NOT NULL,
    deleted boolean NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    deleted_by VARCHAR(255),
    user_id VARCHAR NOT NULL UNIQUE,
    first_name VARCHAR(64) NOT NULL,
    last_name VARCHAR(64) NOT NULL,
    middle_name VARCHAR(64),
    busy boolean NOT NULL DEFAULT FALSE,
    contact_phone VARCHAR(32) NOT NULL UNIQUE,

    CONSTRAINT pk_couriers_history PRIMARY KEY (id, rev),
    CONSTRAINT fk_couriers_history_rev FOREIGN KEY (rev) REFERENCES delivery_bank_history.revinfo (rev)
);

CREATE INDEX IF NOT EXISTS idx_couriers_history_revision ON delivery_bank_history.couriers_history (rev);

CREATE TABLE delivery_bank_history.recipients_history
(
    id BIGINT NOT NULL,
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
    contact_phone VARCHAR(32) NOT NULL UNIQUE,
    office_id VARCHAR,
    person_type VARCHAR(20) NOT NULL,

    CONSTRAINT pk_recipients_history PRIMARY KEY (id, rev),
    CONSTRAINT fk_recipients_history_rev FOREIGN KEY (rev) REFERENCES delivery_bank_history.revinfo (rev)
);

CREATE INDEX IF NOT EXISTS idx_recipients_history_revision ON delivery_bank_history.recipients_history (rev);

CREATE TABLE delivery_bank_history.card_deliveries_history
(
    id VARCHAR NOT NULL,
    rev BIGINT NOT NULL,
    revtype SMALLINT NOT NULL,
    deleted boolean NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    deleted_by VARCHAR(255),
    planned_delivery_time TIMESTAMP WITHOUT TIME ZONE,
    order_id VARCHAR NOT NULL,
    card_type VARCHAR NOT NULL,
    recipient_id BIGINT NOT NULL,
    courier_id VARCHAR,
    courier_contact_phone VARCHAR(32),
    origin_address_id VARCHAR NOT NULL,
    destination_address_id VARCHAR NOT NULL,
    status VARCHAR(20) NOT NULL,
    delivery_duration VARCHAR,
    pickup_date TIMESTAMP WITHOUT TIME ZONE,
    delivered_at TIMESTAMP WITHOUT TIME ZONE,

    CONSTRAINT pk_card_deliveries_history PRIMARY KEY (id, rev),
    CONSTRAINT fk_card_deliveries_history_rev FOREIGN KEY (rev) REFERENCES delivery_bank_history.revinfo (rev)
);

CREATE INDEX IF NOT EXISTS idx_card_deliveries_history_revision ON delivery_bank_history.card_deliveries_history (rev);