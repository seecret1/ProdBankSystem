CREATE SCHEMA IF NOT EXISTS transaction_bank;
CREATE SCHEMA IF NOT EXISTS transaction_bank_history;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
SET search_path TO transaction_bank,transaction_bank_history,public;