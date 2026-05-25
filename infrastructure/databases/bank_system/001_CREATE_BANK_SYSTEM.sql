DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'bank_system') THEN
       CREATE DATABASE bank_system;
END IF;
END $$;