-- Indexes for the most frequent filters and ownership queries.
CREATE INDEX IF NOT EXISTS idx_runs_user_id ON runs(user_id);
CREATE INDEX IF NOT EXISTS idx_runs_game_lower ON runs(LOWER(game));
CREATE INDEX IF NOT EXISTS idx_runs_run_time ON runs(run_time);
CREATE INDEX IF NOT EXISTS idx_runs_pokedex_status ON runs(pokedex_status);

CREATE INDEX IF NOT EXISTS idx_password_reset_token ON password_reset_tokens(token);
CREATE INDEX IF NOT EXISTS idx_password_reset_user_id ON password_reset_tokens(user_id);

-- V5 changes id columns to BIGINT. Keep sequence ownership and next values explicit
-- for databases migrated from the earlier SERIAL definitions.
ALTER SEQUENCE IF EXISTS runs_id_seq AS BIGINT OWNED BY runs.id;
ALTER SEQUENCE IF EXISTS users_id_seq AS BIGINT OWNED BY users.id;
ALTER SEQUENCE IF EXISTS password_reset_tokens_id_seq AS BIGINT OWNED BY password_reset_tokens.id;

SELECT setval('runs_id_seq', COALESCE((SELECT MAX(id) FROM runs), 0) + 1, false)
WHERE to_regclass('runs_id_seq') IS NOT NULL;

SELECT setval('users_id_seq', COALESCE((SELECT MAX(id) FROM users), 0) + 1, false)
WHERE to_regclass('users_id_seq') IS NOT NULL;

SELECT setval('password_reset_tokens_id_seq', COALESCE((SELECT MAX(id) FROM password_reset_tokens), 0) + 1, false)
WHERE to_regclass('password_reset_tokens_id_seq') IS NOT NULL;
