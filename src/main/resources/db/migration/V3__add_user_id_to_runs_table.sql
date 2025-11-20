-- Migration para adicionar coluna user_id e chave estrangeira na tabela 'runs'
ALTER TABLE runs ADD COLUMN user_id BIGINT NOT NULL;

ALTER TABLE runs
ADD CONSTRAINT fk_runs_user_id
FOREIGN KEY (user_id) REFERENCES users(id);

