-- Migration para criar a tabela 'runs' conforme a entidade Run
CREATE TABLE runs (
    id SERIAL PRIMARY KEY,
    game VARCHAR(100) NOT NULL,
    run_time BIGINT NOT NULL, -- armazenado como minutos (DurationConverter)
    pokedex_status INTEGER NOT NULL,
    pokemon_team TEXT, -- armazenado como string separada por vírgula (StringListConverter)
    observation VARCHAR(100)
);

