CREATE TABLE order_bank_history.revinfo
(
    rev BIGSERIAL PRIMARY KEY,
    revtmstmp BIGINT
);

CREATE TABLE order_bank_history.orders_card_history
(
    id VARCHAR NOT NULL,
    rev BIGINT NOT NULL,
    revtype SMALLINT NOT NULL,
    trace_id VARCHAR NOT NULL,
    user_id VARCHAR NOT NULL,
    status VARCHAR(15) NOT NULL,
    deleted boolean NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_by VARCHAR(128),
    comment VARCHAR(255) UNIQUE NOT NULL,
    card_id VARCHAR NOT NULL,
    card_type VARCHAR(10) NOT NULL,
    personal boolean NOT NULL DEFAULT FALSE,
    spending_limit DECIMAL(19, 2) NOT NULL,

    CONSTRAINT fk_orders_card_history_rev FOREIGN KEY (rev) REFERENCES order_bank_history.revinfo (rev)
);