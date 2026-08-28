CREATE SCHEMA IF NOT EXISTS payment_bank;
CREATE SCHEMA IF NOT EXISTS payment_bank_history;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
SET search_path TO payment_bank,payment_bank_history,public;