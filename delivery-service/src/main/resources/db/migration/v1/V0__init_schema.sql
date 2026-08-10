CREATE SCHEMA IF NOT EXISTS delivery_bank;
CREATE SCHEMA IF NOT EXISTS delivery_bank_history;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
SET search_path TO delivery_bank,delivery_bank_history,public;