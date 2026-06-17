# Pokemon Runs Leaderboard API

API REST para gerenciamento de runs de jogos Pokemon com autenticacao JWT, persistencia relacional, filtros, estatisticas e exportacao CSV.

## Objetivo do projeto

Este projeto serve como base tecnica para demonstrar desenvolvimento Java Web com Spring Boot em um cenario monolitico orientado a manutencao evolutiva.

Na fase atual, o foco do repositorio e mostrar com clareza:

- o que ja esta implementado de verdade
- quais requisitos da vaga ja sao atendidos
- quais gaps ainda existem
- qual a ordem planejada de evolucao

## Estado atual real

Hoje a aplicacao entrega:

- API REST com autenticacao JWT
- cadastro, login, troca de senha e reset de senha por token persistido
- CRUD de runs com validacoes e controle de ownership
- filtros por jogo, tempo maximo, status da Pokedex e Pokemon no time
- estatisticas agregadas por jogo e por Pokemon
- exportacao CSV
- consumo de servico REST externo
- consumo de servico SOAP externo
- migrations com Flyway
- execucao local com Docker Compose
- health check com Spring Boot Actuator
- testes unitarios, integracao com H2 e teste com PostgreSQL real via Testcontainers

## Stack atual

- Java 21
- Spring Boot 3.5.6
- Spring Web
- Spring Web Services
- Spring Data JPA
- Spring Security
- Bean Validation
- Flyway
- PostgreSQL
- H2 para parte da suite de testes
- Testcontainers para validacao com PostgreSQL real
- Maven
- Docker e Docker Compose

## Arquitetura e responsabilidades por camada

O projeto segue uma estrutura monolitica simples, com separacao por responsabilidade:

- `controller`: expoe endpoints REST, recebe parametros HTTP e converte respostas para DTOs
- `service`: concentra regras de negocio, validacoes de fluxo e orquestracao entre camadas
- `repository`: acesso a dados com Spring Data JPA e queries customizadas
- `domain`: entidades JPA persistidas no banco
- `dto`: contratos de entrada e saida da API
- `infra/security`: autenticacao JWT, filtro de seguranca e configuracao do Spring Security
- `infra/errors`: tratamento centralizado de erros e respostas padronizadas
- `utils`: conversores auxiliares, como parse de duracao e serializacao de lista em coluna textual
- `src/main/resources/db/migration`: versionamento do schema com Flyway

## Trade-offs tecnicos atuais

- O projeto esta otimizado para PostgreSQL neste momento. A configuracao principal e as migrations assumem PostgreSQL como banco padrao.
- Parte das consultas de relatorio e busca ainda depende de SQL mais aderente ao PostgreSQL, o que explica a fase dedicada a compatibilidade real com MySQL.
- O campo `pokemonTeam` e persistido em uma unica coluna textual via conversor. Isso simplifica o modelo atual, mas aumenta o custo de portabilidade e consulta.
- A suite de testes usa H2 para velocidade e um teste separado com PostgreSQL real para cobrir comportamento especifico de banco.
- O profile `local` usa `ddl-auto=update` para produtividade local, enquanto o profile base/producao trabalha com configuracao mais restritiva.

## Requisitos da vaga ja atendidos

Este repositorio ja demonstra, com implementacao real:

- desenvolvimento Java Web com Spring Boot
- manutencao de aplicacao monolitica com separacao em camadas
- API REST autenticada
- consumo de integracoes REST e SOAP como cliente
- persistencia relacional com JPA
- migrations e evolucao de schema com Flyway
- preocupacao com seguranca via JWT, hashing de senha e controle de acesso
- tratamento centralizado de erros
- qualidade basica com testes automatizados
- containerizacao para execucao local
- health check operacional

## Gaps reais em relacao a vaga

Os principais pontos ainda nao demonstrados pelo codigo atual sao:

- compatibilidade real com MySQL
- cache com Redis
- exportacao Excel com Apache POI
- exportacao PDF com iText
- analise continua com SonarQube
- evidencia de deploy mais proxima de ambiente corporativo tradicional, como WAR ou guia para WildFly/JBoss

## Proximas fases planejadas

- Fase 1: compatibilidade real com MySQL, incluindo testes automatizados
- Fase 3: cache com Redis aplicado aos endpoints de leitura e estatisticas
- Fase 4: exportacao Excel com Apache POI
- Fase 5: qualidade continua com SonarQube
- Fase 6: sinal de aderencia a ambiente corporativo tradicional
- Fase 7: exportacao PDF
- Fase 8: compose expandido com stack mais completa
- Fase 9: material final de demonstracao para entrevista

## Estrutura principal

```text
src/main/java/pokemon/runs/time/leaderboard/
|- controller/
|- domain/
|- dto/
|- infra/
|  |- config/
|  |- errors/
|  \- security/
|- repository/
|- service/
\- utils/
```

## Profiles disponiveis

- `local`: desenvolvimento local com defaults para PostgreSQL, JWT e CORS
- `prod`: exige configuracao explicita de banco, segredo JWT e origens CORS
- `test`: usado pela suite automatizada com H2 em memoria

## Como executar localmente

### Pre-requisitos

- Docker
- Docker Compose

### Variaveis de ambiente

Use `.env.example` como referencia. Para execucao local, o fluxo esperado hoje e:

```bash
SPRING_PROFILES_ACTIVE=local
```

As principais variaveis usadas pela aplicacao sao:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_JPA_HIBERNATE_DDL_AUTO`
- `API_SECURITY_TOKEN_SECRET`
- `CORS_ALLOWED_ORIGINS`
- `INTEGRATION_HTTP_CONNECT_TIMEOUT`
- `INTEGRATION_HTTP_READ_TIMEOUT`
- `INTEGRATION_REST_POKE_API_BASE_URL`
- `INTEGRATION_SOAP_NUMBER_CONVERSION_URL`

### Subir com Docker Compose

```bash
docker compose up --build
```

O ambiente atual sobe:

- `postgres`
- `leaderboard-app`

### Health check

```bash
curl http://localhost:8080/actuator/health
```

## Integracoes externas da Fase 2

O projeto agora demonstra um caso de uso simples e isolado de integracao externa:

- REST: consulta dados do Pokemon na PokeAPI
- SOAP: converte o numero da Pokedex para texto usando o servico Number Conversion

Fluxo exposto:

- `GET /integrations/pokemon/{pokemon}`

Exemplo:

```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/integrations/pokemon/pikachu
```

Resposta esperada:

```json
{
  "pokemon": "pikachu",
  "pokedexNumber": 25,
  "pokedexNumberInWords": "twenty five",
  "baseExperience": 112,
  "types": ["electric"]
}
```

Comportamento de falha:

- falha ou indisponibilidade externa retorna erro tratado e consistente
- payload externo invalido retorna `502 Bad Gateway`
- timeout externo e tratado na camada de cliente pelas configuracoes de timeout

## Endpoints principais

### Publicos

- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/forgot-password`
- `POST /auth/reset-password`
- `GET /actuator/health`

### Protegidos

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

## Fluxo rapido de validacao

### Registrar usuario

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "ash",
    "password": "pikachu123",
    "email": "ash@pokemon.com"
  }'
```

### Login

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "ash",
    "password": "pikachu123"
  }' | jq -r .token)
```

### Criar run

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

## Testes

Executar a suite principal:

```bash
./mvnw test
```

Cobertura atual de validacao:

- testes unitarios de servicos, DTOs, seguranca e configuracao
- testes de integracao de controllers
- testes de repositorio com H2 para fluxo rapido
- teste com PostgreSQL real via Testcontainers para migrations e queries especificas
- testes de integracao cobrindo cenario feliz e falhas das integracoes REST e SOAP

## Infra atual

O `compose.yaml` atual sobe apenas PostgreSQL e a aplicacao Spring Boot. Redis, MySQL e SonarQube ainda nao fazem parte do ambiente local padrao porque pertencem a fases futuras.

## Checklist de entregas tecnicas

O acompanhamento das fases esta em `CHECKLIST.md`. A Fase 0 cobre documentacao de baseline, narrativa tecnica e alinhamento entre README, estado real do codigo e proximas entregas.

## Resumo para entrevista

Hoje o projeto demonstra uma API Spring Boot monolitica, autenticada e testada, com PostgreSQL, Flyway, operacao local por containers e consumo de servicos REST e SOAP externos. O proximo passo planejado e reduzir os gaps mais aderentes a vaga: MySQL, Redis e qualidade continua.
