CREATE TABLE transaction_bank_history.revinfo
(
    rev BIGSERIAL PRIMARY KEY,
    revtmstmp BIGINT
);

CREATE TABLE transaction_bank_history.transactions_history
(
    id VARCHAR NOT NULL,
    rev BIGINT NOT NULL,
    revtype SMALLINT NOT NULL,
    user_id VARCHAR NOT NULL,
    payment_id VARCHAR NOT NULL,
    source_invoice_id VARCHAR NOT NULL,
    destination_invoice_id VARCHAR NOT NULL,
    amount NUMERIC(18,2) NOT NULL,
    currency VARCHAR NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),

    CONSTRAINT fk_transactions_history_rev FOREIGN KEY (rev) REFERENCES transaction_bank_history.revinfo (rev)
);