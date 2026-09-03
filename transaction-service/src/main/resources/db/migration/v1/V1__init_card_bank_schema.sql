CREATE TABLE transaction_bank.transactions
(
    id VARCHAR PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id VARCHAR NOT NULL,
    payment_id VARCHAR NOT NULL,
    source_invoice_id VARCHAR NOT NULL,
    destination_invoice_id VARCHAR NOT NULL,
    amount NUMERIC(18,2) NOT NULL,
    currency VARCHAR NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc')
);