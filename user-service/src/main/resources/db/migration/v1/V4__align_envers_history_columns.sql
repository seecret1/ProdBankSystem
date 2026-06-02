ALTER TABLE person_bank_history.countries_history RENAME COLUMN revision TO rev;
ALTER TABLE person_bank_history.countries_history RENAME COLUMN revision_type TO revtype;

ALTER TABLE person_bank_history.addresses_history RENAME COLUMN revision TO rev;
ALTER TABLE person_bank_history.addresses_history RENAME COLUMN revision_type TO revtype;

ALTER TABLE person_bank_history.users_history RENAME COLUMN revision TO rev;
ALTER TABLE person_bank_history.users_history RENAME COLUMN revision_type TO revtype;

ALTER TABLE person_bank_history.individuals_history RENAME COLUMN revision TO rev;
ALTER TABLE person_bank_history.individuals_history RENAME COLUMN revision_type TO revtype;
