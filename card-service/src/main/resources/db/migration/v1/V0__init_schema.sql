CREATE SCHEMA IF NOT EXISTS card_bank;
CREATE SCHEMA IF NOT EXISTS card_bank_history;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
SET search_path TO card_bank,card_bank_history,public;