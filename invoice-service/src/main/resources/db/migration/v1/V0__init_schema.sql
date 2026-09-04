CREATE SCHEMA IF NOT EXISTS invoice_bank;
CREATE SCHEMA IF NOT EXISTS invoice_bank_history;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
SET search_path TO invoice_bank,invoice_bank_history,public;