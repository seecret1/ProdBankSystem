CREATE TABLE office_bank.countries
(
    id SERIAL PRIMARY KEY,
    deleted boolean NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    name VARCHAR(128) NOT NULL,
    code VARCHAR(3) NOT NULL
);

CREATE TABLE office_bank.addresses
(
    id VARCHAR PRIMARY KEY DEFAULT uuid_generate_v4(),
    deleted boolean NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    deleted_by VARCHAR(128),
    country_id INTEGER NOT NULL REFERENCES office_bank.countries (id),
    address VARCHAR(128) NOT NULL,
    zip_code VARCHAR(32) NOT NULL,
    city VARCHAR(64) NOT NULL,

    CONSTRAINT uk_address_city_country UNIQUE (city, address, country_id)
);

CREATE INDEX idx_addresses_city_country ON office_bank.addresses (city, country_id);

CREATE TABLE office_bank.offices
(
    id VARCHAR PRIMARY KEY DEFAULT uuid_generate_v4(),
    deleted boolean NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    deleted_by VARCHAR(255),
    name VARCHAR(64) NOT NULL,
    main boolean NOT NULL DEFAULT FALSE,
    contact_phone VARCHAR(64) NOT NULL UNIQUE,
    schedule_json JSONB NOT NULL,
    active boolean NOT NULL DEFAULT TRUE,
    address_id VARCHAR NOT NULL REFERENCES office_bank.addresses (id),
    owner_id VARCHAR(64) NOT NULL
);