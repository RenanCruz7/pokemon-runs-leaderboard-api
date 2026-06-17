CREATE TABLE runs (
    id BIGSERIAL PRIMARY KEY,
    game VARCHAR(100) NOT NULL,
    run_time BIGINT NOT NULL,
    pokedex_status INTEGER NOT NULL,
    pokemon_team TEXT,
    observation VARCHAR(100)
);
