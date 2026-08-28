CREATE TABLE order_bank_history.revinfo
(
    rev BIGSERIAL PRIMARY KEY,
    revtmstmp BIGINT
);

CREATE TABLE order_bank_history.countries_history
(
    id INTEGER NOT NULL,
    rev BIGINT NOT NULL,
    revtype SMALLINT NOT NULL,
    deleted boolean NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    name VARCHAR(128) NOT NULL,
    code VARCHAR(3) NOT NULL,

    CONSTRAINT pk_countries_history PRIMARY KEY (id, rev),
    CONSTRAINT fk_countries_history_rev FOREIGN KEY (rev) REFERENCES order_bank_history.revinfo (rev)
);

CREATE INDEX IF NOT EXISTS idx_countries_history_revision ON order_bank_history.countries_history (rev);

CREATE TABLE order_bank_history.addresses_history
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
    city VARCHAR(64) NOT NULL,

    CONSTRAINT pk_addresses_history PRIMARY KEY (id, rev),
    CONSTRAINT fk_addresses_history_rev FOREIGN KEY (rev) REFERENCES order_bank_history.revinfo (rev)
);

CREATE INDEX IF NOT EXISTS idx_addresses_history_revision ON order_bank_history.addresses_history (rev);

CREATE TABLE order_bank_history.orders_card_delivery_history
(
    id VARCHAR NOT NULL,
    rev BIGINT NOT NULL,
    revtype SMALLINT NOT NULL,
    planned_delivery_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    first_name VARCHAR(64),
    last_name VARCHAR(64),
    middle_name VARCHAR(64),
    contact_phone VARCHAR(32),
    office_id VARCHAR,
    original_address_id VARCHAR,
    destination_address_id VARCHAR NOT NULL,
    person_type VARCHAR(20),

    CONSTRAINT pk_orders_card_delivery_history PRIMARY KEY (id, rev),
    CONSTRAINT fk_orders_card_delivery_history_rev FOREIGN KEY (rev) REFERENCES order_bank_history.revinfo (rev)
);

CREATE INDEX IF NOT EXISTS idx_orders_card_history_revision ON order_bank_history.orders_card_delivery_history (rev);

CREATE TABLE order_bank_history.orders_card_history
(
    id VARCHAR NOT NULL,
    rev BIGINT NOT NULL,
    revtype SMALLINT NOT NULL,
    trace_id VARCHAR NOT NULL,
    user_id VARCHAR NOT NULL,
    status VARCHAR(15) NOT NULL,
    deleted boolean NOT NULL DEFAULT FALSE,
    request_timestamp TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    deleted_by VARCHAR(128),
    comment VARCHAR(255) NOT NULL,
    card_id VARCHAR NOT NULL,
    card_type VARCHAR(10) NOT NULL,
    invoice_id VARCHAR UNIQUE,
    card_receiving_method VARCHAR(20) NOT NULL,
    order_card_delivery_id VARCHAR,
    currency VARCHAR,
    balance NUMERIC(18, 4),

    CONSTRAINT pk_orders_card_history PRIMARY KEY (id, rev),
    CONSTRAINT fk_orders_card_history_rev FOREIGN KEY (rev) REFERENCES order_bank_history.revinfo (rev)
);

CREATE INDEX IF NOT EXISTS idx_orders_card_history_revision ON order_bank_history.orders_card_history (rev);