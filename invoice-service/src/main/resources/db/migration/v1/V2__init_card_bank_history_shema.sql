CREATE TABLE invoice_bank_history.revinfo
(
    rev BIGSERIAL PRIMARY KEY,
    revtmstmp BIGINT
);

CREATE TABLE invoice_bank_history.operation_history
(
    id VARCHAR NOT NULL,
    rev BIGINT NOT NULL,
    revtype SMALLINT NOT NULL,
    operation_type VARCHAR(50) NOT NULL,
    amount_from NUMERIC(18,2) NOT NULL DEFAULT 0,
    amount_to NUMERIC(18,2),
    commission_percent NUMERIC(5,2) NOT NULL,
    commission_amount NUMERIC(18,2) DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),

    CONSTRAINT fk_operation_history_rev FOREIGN KEY (rev) REFERENCES invoice_bank_history.revinfo (rev)
);

CREATE TABLE invoice_bank_history.card_invoices_history
(
    id VARCHAR NOT NULL,
    rev BIGINT NOT NULL,
    revtype SMALLINT NOT NULL,
    user_id VARCHAR NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    deleted_by VARCHAR(128),
    card_id VARCHAR(120) NOT NULL,
    status VARCHAR(30) NOT NULL,
    invoice_number VARCHAR(50) NOT NULL,
    currency VARCHAR NOT NULL,
    balance NUMERIC(18,2) NOT NULL DEFAULT 0,
    spending_limit NUMERIC(18,2) NOT NULL,
    free_limit NUMERIC(18,2) NOT NULL,
    operation_id VARCHAR,

    CONSTRAINT fk_card_invoices_history_rev FOREIGN KEY (rev) REFERENCES invoice_bank_history.revinfo (rev)
);