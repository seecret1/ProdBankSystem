DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'bank') THEN
       CREATE DATABASE bank;
END IF;
END $$;