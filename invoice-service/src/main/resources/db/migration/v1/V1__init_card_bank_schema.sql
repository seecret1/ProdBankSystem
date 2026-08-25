CREATE TABLE invoice_bank.operation
(
    id VARCHAR PRIMARY KEY DEFAULT uuid_generate_v4(),
    operation_type VARCHAR(50) NOT NULL,
    amount_from NUMERIC(18,2) NOT NULL DEFAULT 0,
    amount_to NUMERIC(18,2),
    commission_percent NUMERIC(5,2) NOT NULL,
    commission_amount NUMERIC(18,2) DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc')
);

CREATE TABLE invoice_bank.card_invoices
(
    id VARCHAR PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id VARCHAR NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    deleted_by VARCHAR(128),
    card_id VARCHAR(120) UNIQUE NOT NULL,
    status VARCHAR(30) NOT NULL,
    invoice_number VARCHAR(50) UNIQUE NOT NULL,
    currency CHAR(3) NOT NULL,
    balance NUMERIC(18,2) NOT NULL DEFAULT 0,
    spending_limit NUMERIC(18,2) NOT NULL,
    operation_id VARCHAR REFERENCES invoice_bank.operation(id)
);