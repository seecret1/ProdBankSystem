CREATE TABLE order_bank.orders_card
(
    id VARCHAR PRIMARY KEY DEFAULT uuid_generate_v4(),
    trace_id VARCHAR NOT NULL,
    user_id VARCHAR NOT NULL,
    status VARCHAR(15) NOT NULL,
    deleted boolean NOT NULL DEFAULT FALSE,
    request_timestamp TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_by VARCHAR(128),
    comment VARCHAR(255) NOT NULL,
    card_id VARCHAR NOT NULL,
    card_type VARCHAR(20) NOT NULL,
    spending_limit DECIMAL(19, 2) NOT NULL
);