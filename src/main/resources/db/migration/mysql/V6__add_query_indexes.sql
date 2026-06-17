CREATE INDEX idx_runs_user_id ON runs(user_id);
CREATE INDEX idx_runs_game ON runs(game);
CREATE INDEX idx_runs_run_time ON runs(run_time);
CREATE INDEX idx_runs_pokedex_status ON runs(pokedex_status);
CREATE INDEX idx_password_reset_token ON password_reset_tokens(token);
CREATE INDEX idx_password_reset_user_id ON password_reset_tokens(user_id);
CREATE INDEX idx_password_reset_expiry ON password_reset_tokens(expiry_date);
