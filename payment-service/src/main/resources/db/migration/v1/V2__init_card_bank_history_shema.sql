CREATE TABLE payment_bank_history.revinfo
(
    rev BIGSERIAL PRIMARY KEY,
    revtmstmp BIGINT
);

CREATE TABLE payment_bank_history.payments_history
(
    id VARCHAR,
    rev BIGINT NOT NULL,
    revtype SMALLINT NOT NULL,
    user_id VARCHAR NOT NULL,
    source_invoice_id VARCHAR NOT NULL,
    destination_invoice_id VARCHAR,
    amount NUMERIC(18,2) NOT NULL,
    currency VARCHAR NOT NULL,
    payment_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),

    CONSTRAINT fk_payments_history_rev FOREIGN KEY (rev) REFERENCES payment_bank_history.revinfo (rev)
);