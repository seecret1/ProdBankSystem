ALTER TABLE person_bank_history.users_history
    ALTER COLUMN address_id TYPE VARCHAR(255) USING address_id::varchar;

ALTER TABLE person_bank_history.individuals_history
    ALTER COLUMN user_id TYPE VARCHAR(255) USING user_id::varchar;
