CREATE TABLE order_bank.orders_card_delivery
(
    id VARCHAR PRIMARY KEY DEFAULT uuid_generate_v4(),
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
    planned_delivery_time TIMESTAMP WITHOUT TIME ZONE,
    delivery_id VARCHAR NOT NULL,
    first_name VARCHAR(64) NOT NULL,
    last_name VARCHAR(64) NOT NULL,
    middle_name VARCHAR(64),
    contact_phone VARCHAR(32) NOT NULL,
    office_id VARCHAR,
    person_type VARCHAR(20) NOT NULL
);

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
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    deleted_by VARCHAR(128),
    comment VARCHAR(255) NOT NULL,
    card_id VARCHAR NOT NULL,
    card_type VARCHAR(20) NOT NULL,
    invoice_id VARCHAR UNIQUE,
    card_receiving_method VARCHAR(20) NOT NULL,
    order_card_delivery_id VARCHAR REFERENCES order_bank.orders_card_delivery (id)
);