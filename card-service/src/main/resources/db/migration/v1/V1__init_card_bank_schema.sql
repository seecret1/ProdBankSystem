CREATE TABLE card_bank.cards
(
    id VARCHAR PRIMARY KEY DEFAULT uuid_generate_v4(),
    deleted boolean NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_by VARCHAR(128),
    number VARCHAR(120) UNIQUE NOT NULL,
    number_hash VARCHAR(64) NOT NULL,
    type VARCHAR(20) NOT NULL,
    date_activation DATE NOT NULL,
    date_expiry DATE NOT NULL,
    status VARCHAR(10) NOT NULL,
    balance DECIMAL(19, 2) NOT NULL,
    spending_limit DECIMAL(19, 2) NOT NULL,
    user_id VARCHAR NOT NULL
);