-- Migration para alterar colunas id de SERIAL para BIGSERIAL

-- Alterar coluna id da tabela runs
ALTER TABLE runs ALTER COLUMN id TYPE BIGINT;

-- Alterar coluna id da tabela users
ALTER TABLE users ALTER COLUMN id TYPE BIGINT;

-- Alterar coluna id da tabela password_reset_tokens (se existir)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'password_reset_tokens') THEN
        ALTER TABLE password_reset_tokens ALTER COLUMN id TYPE BIGINT;
    END IF;
END $$;

