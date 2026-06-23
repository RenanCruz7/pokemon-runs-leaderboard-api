# Pokemon Runs Leaderboard API

API REST desenvolvida com Spring Boot para gerenciamento de runs de jogos Pokemon. O projeto demonstra autenticacao JWT, persistencia relacional, filtros, estatisticas, exportacao de dados, cache com Redis e integracoes externas REST e SOAP.

## Visao Geral

Principais capacidades da aplicacao:

- autenticacao JWT com Spring Security
- cadastro, login, troca de senha e reset de senha por token
- CRUD de runs com controle de ownership
- filtros por jogo, tempo maximo, status da Pokedex e Pokemon no time
- estatisticas agregadas por jogo e por Pokemon
- exportacao em CSV e Excel
- integracao com PokeAPI e com servico SOAP de conversao numerica
- suporte a PostgreSQL e MySQL
- cache com Redis para endpoints de leitura
- migrations versionadas com Flyway
- health check com Spring Boot Actuator
- testes unitarios, de integracao e com Testcontainers

## Stack Tecnica

- Java 21
- Spring Boot 3.5.6
- Spring Web
- Spring Security
- Spring Data JPA
- Spring Validation
- Spring Web Services
- Spring Boot Actuator
- PostgreSQL
- MySQL
- Redis
- Flyway
- Apache POI
- Maven
- Docker Compose
- JaCoCo
- SonarQube
- Testcontainers

## Arquitetura

O projeto segue uma arquitetura monolitica em camadas, com separacao clara de responsabilidades:

- `controller`: endpoints REST e contrato HTTP
- `service`: regras de negocio e orquestracao
- `repository`: acesso a dados com Spring Data JPA
- `domain`: entidades persistidas
- `dto`: contratos de entrada e saida
- `infra/security`: autenticacao JWT, filtro e configuracao de seguranca
- `infra/errors`: tratamento centralizado de erros
- `infra/config`: configuracoes de CORS e integracoes externas
- `db/migration`: versionamento de schema com Flyway

Decisoes tecnicas relevantes:

- autenticacao stateless com JWT
- migrations separadas por vendor para PostgreSQL e MySQL
- cache aplicado em consultas repetidas e estatisticas, com invalidacao por mutacao
- integracoes externas com timeout configuravel e tratamento consistente de falhas
- suite de testes combinando H2 para rapidez e Testcontainers para validar comportamento real de banco

## Como Executar

### Pre-requisitos

- Java 21
- Docker e Docker Compose
- `curl` e `jq` opcionais para os exemplos de validacao manual

### Opcao 1: Executar com Docker Compose

1. Crie um arquivo `.env` a partir de `.env.example`.
2. Suba a stack:

```bash
docker compose up --build
```

3. Verifique o health check:

```bash
curl http://localhost:8080/actuator/health
```

Servicos da stack local:

- `leaderboard-app`
- `postgres`
- `mysql`
- `redis`
- `sonarqube-db`
- `sonarqube`

Por padrao, a aplicacao sobe com o profile `local` usando PostgreSQL.

### Opcao 2: Executar localmente com Maven

Para subir apenas a infraestrutura:

```bash
docker compose up -d postgres redis
```

Depois execute a aplicacao:

```bash
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

Para rodar com MySQL:

```bash
docker compose up -d mysql redis
SPRING_PROFILES_ACTIVE=mysql \
SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/leaderboard_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
SPRING_DATASOURCE_USERNAME=leaderboard \
SPRING_DATASOURCE_PASSWORD=leaderboard \
./mvnw spring-boot:run
```

Se quiser usar Redis fora do container da aplicacao, defina tambem:

```bash
SPRING_CACHE_TYPE=redis
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379
```

## Profiles

- `local`: desenvolvimento local com defaults para PostgreSQL e configuracoes mais produtivas
- `mysql`: executa a aplicacao com dialeto e datasource para MySQL
- `prod`: exige configuracao explicita de banco, segredo JWT e CORS
- `test`: usado pela suite automatizada

## Variaveis de Ambiente

O arquivo `.env.example` documenta os valores esperados. As variaveis abaixo sao as mais importantes para operacao e para conversa em entrevista tecnica.

### Aplicacao

| Variavel | Finalidade | Exemplo / Default |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | Seleciona o profile ativo | `local`, `mysql`, `prod` |
| `PORT` | Porta HTTP da aplicacao | `8080` |
| `SPRING_DATASOURCE_URL` | URL de conexao do banco | `jdbc:postgresql://localhost:5432/leaderboard_db` |
| `SPRING_DATASOURCE_USERNAME` | Usuario do banco | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Senha do banco | `postgres` |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Estrategia de schema do Hibernate | `update` no local, `validate` no base/prod |
| `API_SECURITY_TOKEN_SECRET` | Chave usada para assinar o JWT | obrigatoria em producao |
| `CORS_ALLOWED_ORIGINS` | Origens permitidas para CORS | `http://localhost:*` |

### Cache e Redis

| Variavel | Finalidade | Exemplo / Default |
| --- | --- | --- |
| `SPRING_CACHE_TYPE` | Liga ou desliga cache | `none` ou `redis` |
| `SPRING_CACHE_REDIS_TTL` | TTL das entradas de cache | `10m` |
| `SPRING_DATA_REDIS_HOST` | Host do Redis | `redis` no Compose, `localhost` fora dele |
| `SPRING_DATA_REDIS_PORT` | Porta do Redis | `6379` |

### Integracoes externas

| Variavel | Finalidade | Exemplo / Default |
| --- | --- | --- |
| `INTEGRATION_HTTP_CONNECT_TIMEOUT` | Timeout de conexao HTTP | `3s` |
| `INTEGRATION_HTTP_READ_TIMEOUT` | Timeout de leitura HTTP | `5s` |
| `INTEGRATION_REST_POKE_API_BASE_URL` | Base URL da PokeAPI | `https://pokeapi.co/api/v2` |
| `INTEGRATION_SOAP_NUMBER_CONVERSION_URL` | Endpoint SOAP de conversao numerica | `https://www.dataaccess.com/webservicesserver/NumberConversion.wso` |

### Variaveis da stack Docker

Essas variaveis sao usadas principalmente pelos containers auxiliares do `compose.yaml`:

- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `MYSQL_DATABASE`
- `MYSQL_USER`
- `MYSQL_PASSWORD`
- `MYSQL_ROOT_PASSWORD`
- `SONARQUBE_DB_NAME`
- `SONARQUBE_DB_USER`
- `SONARQUBE_DB_PASSWORD`
- `SONAR_HOST_URL`
- `SONAR_TOKEN`

Observacao importante: `SPRING_DATASOURCE_*` configura a aplicacao Spring; `POSTGRES_*`, `MYSQL_*` e `SONARQUBE_DB_*` configuram os containers de infraestrutura.

## Endpoints Principais

### Publicos

- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/forgot-password`
- `POST /auth/reset-password`
- `GET /actuator/health`

### Protegidos por JWT

- `PATCH /auth/change-password`
- `GET /integrations/pokemon/{pokemon}`
- `POST /runs`
- `GET /runs`
- `GET /runs/me`
- `GET /runs/{id}`
- `PATCH /runs/{id}`
- `DELETE /runs/{id}`
- `GET /runs/game/{game}`
- `GET /runs/fastest?maxTime=HH:MM`
- `GET /runs/pokedex?minStatus=100`
- `GET /runs/team?pokemon=Pikachu`
- `GET /runs/stats/count-by-game`
- `GET /runs/stats/avg-time-by-game`
- `GET /runs/stats/top-pokemons`
- `GET /runs/export/csv`
- `GET /runs/export/excel`

## Fluxo Rapido de Uso

### 1. Registrar usuario

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "ash",
    "password": "pikachu123",
    "email": "ash@pokemon.com"
  }'
```

### 2. Fazer login

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "ash",
    "password": "pikachu123"
  }' | jq -r .token)
```

### 3. Criar uma run

```bash
curl -X POST http://localhost:8080/runs \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "game": "Pokemon Red",
    "runTime": "2:30",
    "pokedexStatus": 150,
    "pokemonTeam": ["Pikachu", "Charizard", "Blastoise"],
    "observation": "First speedrun attempt"
  }'
```

### 4. Consultar integracao externa

```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/integrations/pokemon/pikachu
```

## Testes e Qualidade

Executar testes:

```bash
./mvnw test
```

Executar validacao completa com cobertura:

```bash
./mvnw verify
```

Rodar analise SonarQube local:

```bash
make sonar-up
make sonar
make sonar-down
```

Observacoes:

- os testes usam o profile `test`
- parte da suite depende de Docker para Testcontainers
- o SonarQube local fica disponivel em `http://localhost:9000`

## Pontos Relevantes Para Entrevista Tecnica

- seguranca: autenticacao stateless com JWT, rotas protegidas e senhas com hashing
- persistencia: uso de JPA com Flyway e compatibilidade real entre PostgreSQL e MySQL
- cache: Redis aplicado em endpoints de leitura e estatisticas, com foco em consistencia na invalidacao
- integracoes: combinacao de cliente REST e cliente SOAP com timeouts configuraveis e tratamento centralizado de erro
- arquitetura: separacao por camadas para manter o monolito simples e evolutivo
- observabilidade basica: endpoint de health check e stack local containerizada
- qualidade: testes de unidade, integracao, repositorio e cenarios com bancos reais via Testcontainers
- exportacao de dados: suporte a CSV e Excel usando Apache POI

## Resumo

Este projeto apresenta uma API Spring Boot pronta para demonstracao tecnica, cobrindo autenticacao, persistencia relacional, cache, integracoes externas, exportacao de dados, testes automatizados e execucao local com containers.
