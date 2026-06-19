CREATE TABLE card_bank_history.revinfo
(
    rev BIGSERIAL PRIMARY KEY,
    revtmstmp BIGINT
);

CREATE TABLE card_bank_history.cards_history
(
    id VARCHAR NOT NULL,
    rev BIGINT NOT NULL,
    revtype SMALLINT NOT NULL,
    deleted boolean NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    deleted_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    number VARCHAR(19) UNIQUE NOT NULL,
    number_hash VARCHAR(128) NOT NULL,
    date_activation DATE NOT NULL,
    date_expiry DATE NOT NULL,
    status VARCHAR(10) NOT NULL,
    balance DECIMAL(19, 2) NOT NULL,
    user_id VARCHAR NOT NULL,

    CONSTRAINT fk_cards_history_rev FOREIGN KEY (rev) REFERENCES card_bank_history.revinfo (rev)
);