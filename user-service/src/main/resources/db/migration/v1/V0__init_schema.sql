CREATE SCHEMA IF NOT EXISTS person_bank;
CREATE SCHEMA IF NOT EXISTS person_bank_history;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
SET search_path TO person_bank,person_bank_history,public;