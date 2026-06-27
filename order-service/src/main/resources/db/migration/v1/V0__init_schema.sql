CREATE SCHEMA IF NOT EXISTS order_bank;
CREATE SCHEMA IF NOT EXISTS order_bank_history;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
SET search_path TO order_bank,order_bank_history,public;