CREATE TABLE bank.countries
(
    id SERIAL PRIMARY KEY,
    deleted boolean NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    name VARCHAR(128) NOT NULL,
    code VARCHAR(3) NOT NULL
);

CREATE TABLE bank.addresses
(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    deleted boolean NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_by VARCHAR(128),
    country_id INTEGER NOT NULL REFERENCES bank.countries (id),
    address VARCHAR(128) NOT NULL,
    zip_code VARCHAR(32) NOT NULL,
    city VARCHAR(64) NOT NULL
);

CREATE TABLE bank.users
(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
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
    address_id UUID NOT NULL REFERENCES bank.addresses (id)
);

CREATE TABLE bank.individuals
(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    deleted boolean NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_by VARCHAR(255),
    passport_number VARCHAR(64) NOT NULL,
    phone_number VARCHAR(64) NOT NULL,
    user_id UUID NOT NULL REFERENCES person.users (id)
);