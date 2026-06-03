CREATE TABLE person_bank.countries
(
    id SERIAL PRIMARY KEY,
    deleted boolean NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    name VARCHAR(128) NOT NULL,
    code VARCHAR(3) NOT NULL
);

CREATE TABLE person_bank.addresses
(
    id VARCHAR PRIMARY KEY DEFAULT uuid_generate_v4(),
    deleted boolean NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_by VARCHAR(128),
    country_id INTEGER NOT NULL REFERENCES person_bank.countries (id),
    address VARCHAR(128) NOT NULL,
    zip_code VARCHAR(32) NOT NULL,
    city VARCHAR(64) NOT NULL
);

CREATE TABLE person_bank.users
(
    id VARCHAR PRIMARY KEY DEFAULT uuid_generate_v4(),
    deleted boolean NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_by VARCHAR(255),
    username VARCHAR(100) NOT NULL,
    status VARCHAR(15) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(64) NOT NULL,
    last_name VARCHAR(64) NOT NULL,
    middle_name VARCHAR(64),
    birth_date DATE NOT NULL,
    role VARCHAR(15) NOT NULL
);

CREATE TABLE person_bank.individuals
(
    id VARCHAR PRIMARY KEY DEFAULT uuid_generate_v4(),
    deleted boolean NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_by VARCHAR(255),
    passport_number VARCHAR(64) NOT NULL,
    phone_number VARCHAR(64) NOT NULL,
    user_id VARCHAR NOT NULL REFERENCES person_bank.users (id),
    address_id VARCHAR NOT NULL REFERENCES person_bank.addresses (id)
);

CREATE TABLE person_bank.refresh_tokens
(
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL,
    expiry_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    revoked boolean NOT NULL DEFAULT FALSE,
    user_id VARCHAR NOT NULL REFERENCES person_bank.users (id)
);