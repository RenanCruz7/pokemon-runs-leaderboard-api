# 🎮 Pokémon Runs Leaderboard API

API REST para gerenciar um leaderboard de speedruns de jogos Pokémon com autenticação JWT.

## ✨ Características

- 🔐 Autenticação JWT
- 🏃 CRUD completo de runs
- 📊 Estatísticas e filtros avançados
- 🌐 **CORS habilitado para desenvolvimento frontend**
- 🐘 PostgreSQL como banco de dados
- 🔄 Migrations com Flyway
- 🐳 Deploy com Docker

---

### 📋 Pré-requisitos
- Docker instalado
- Docker Compose instalado

### 🔧 Passo a Passo

#### 1. Clone o repositório (se ainda não fez)
```bash
cd /home/renan/Documentos/pokemon-runs-leaderboard-api
```

#### 2. Verifique o arquivo .env
O arquivo `.env` já foi criado com as configurações padrão. Se quiser alterar alguma configuração, edite o arquivo:
```bash
nano .env
```

#### 3. Inicie o projeto com Docker Compose

**Comando principal (build e start):**
```bash
docker compose up --build
```

Este comando irá:
- ✅ Fazer o build da aplicação Java
- ✅ Criar o container do PostgreSQL
- ✅ Criar o container da aplicação
- ✅ Executar as migrations do Flyway automaticamente
- ✅ Iniciar a API na porta 8080

**Para rodar em background (detached mode):**
```bash
docker compose up --build -d
```

#### 4. Verificar se está rodando
```bash
# Ver logs em tempo real
docker compose logs -f

# Ver apenas logs da aplicação
docker compose logs -f leaderboard-app

# Ver status dos containers
docker compose ps
```

#### 5. Testar a API

A API estará disponível em: `http://localhost:8080`

**Teste rápido:**
```bash
# Health check
curl http://localhost:8080/auth/login
```

### 🧪 Testando a Autenticação e Criação de Runs

#### 1. Registrar um usuário
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "ash",
    "password": "pikachu123",
    "email": "ash@pokemon.com"
  }'
```

#### 2. Fazer login e pegar o token
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "ash",
    "password": "pikachu123"
  }' | jq -r .token)

echo "Seu token: $TOKEN"
```

#### 3. Criar uma run (autenticado)
```bash
curl -X POST http://localhost:8080/runs \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "game": "Pokemon Red",
    "runTime": "2:30",
    "pokedexStatus": 150,
    "pokemonTeam": ["Pikachu", "Charizard", "Blastoise", "Venusaur", "Lapras", "Snorlax"],
    "observation": "First speedrun attempt"
  }'
```

#### 4. Listar todas as runs
```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/runs
```

### 🛑 Parar e Gerenciar Containers

```bash
# Parar os containers (mantém volumes)
docker compose stop

# Parar e remover containers
docker compose down

# Parar, remover containers E volumes (apaga o banco!)
docker compose down -v

# Reiniciar apenas a aplicação (sem rebuild)
docker compose restart leaderboard-app

# Ver logs de erro
docker compose logs leaderboard-app --tail=100
```

### 🔄 Rebuild após mudanças no código

Quando você fizer alterações no código Java:

```bash
# Rebuild e reiniciar
docker compose up --build

# Ou forçar rebuild completo
docker compose build --no-cache
docker compose up
```

### 📊 Endpoints Disponíveis

#### 🔓 Públicos (sem autenticação)
- `POST /auth/register` - Registrar novo usuário
- `POST /auth/login` - Fazer login e obter token JWT

#### 🔒 Protegidos (requer token JWT no header `Authorization: Bearer {token}`)

**Runs:**
- `POST /runs` - Criar nova run
- `GET /runs` - Listar todas as runs (paginado)
- `GET /runs/{id}` - Buscar run por ID
- `PATCH /runs/{id}` - Atualizar run (apenas o dono)
- `DELETE /runs/{id}` - Deletar run (apenas o dono)

**Filtros:**
- `GET /runs/game/{game}` - Filtrar por jogo
- `GET /runs/fastest?maxTime=PT2H` - Runs mais rápidas
- `GET /runs/pokedex?minStatus=100` - Filtrar por Pokédex
- `GET /runs/team?pokemon=Pikachu` - Filtrar por Pokémon no time

**Estatísticas:**
- `GET /runs/stats/count-by-game` - Contagem de runs por jogo
- `GET /runs/stats/avg-time-by-game` - Tempo médio por jogo
- `GET /runs/stats/top-pokemons` - Top 10 Pokémon mais usados

**Exportação:**
- `GET /runs/export/csv` - Exportar runs em CSV

### 🗄️ Acessar o PostgreSQL

```bash
# Via Docker
docker compose exec postgres psql -U postgres -d leaderboard_db

# Ou via host (se tiver psql instalado)
psql -h localhost -U postgres -d leaderboard_db
```

Senha padrão: `postgres`

**Queries úteis:**
```sql
-- Ver todos os usuários
SELECT * FROM users;

-- Ver todas as runs com usuário
SELECT r.id, r.game, r.run_time, u.username 
FROM runs r 
JOIN users u ON r.user_id = u.id;

-- Ver runs de um usuário específico
SELECT * FROM runs WHERE user_id = 1;
```

### 🐛 Troubleshooting

**Problema: Porta 8080 já em uso**
```bash
# Mudar porta no compose.yaml
# De: '8080:8080'
# Para: '8081:8080'
```

**Problema: Erro de conexão com banco**
```bash
# Ver logs do postgres
docker compose logs postgres

# Verificar se postgres está healthy
docker compose ps
```

**Problema: Migrations falhando**
```bash
# Apagar volume e recriar
docker compose down -v
docker compose up --build
```

**Problema: Token JWT inválido**
- Verifique se a variável `API_SECURITY_TOKEN_SECRET` está no `.env`
- Recrie o token fazendo login novamente

### 📦 Estrutura do Projeto

```
pokemon-runs-leaderboard-api/
├── src/
│   └── main/
│       ├── java/pokemon/runs/time/leaderboard/
│       │   ├── controller/          # Controllers REST
│       │   ├── service/             # Lógica de negócio
│       │   ├── repository/          # Acesso ao banco
│       │   ├── domain/              # Entidades JPA
│       │   ├── dto/                 # Data Transfer Objects
│       │   ├── infra/security/      # Configuração de segurança JWT
│       │   └── utils/               # Conversores e utilitários
│       └── resources/
│           ├── application.properties
│           └── db/migration/        # Migrations Flyway
├── compose.yaml                     # Docker Compose config
├── Dockerfile                       # Build da aplicação
├── .env                            # Variáveis de ambiente
└── pom.xml                         # Dependências Maven
```

### ✨ Tecnologias

- ☕ Java 21
- 🍃 Spring Boot 3.5.6
- 🔐 Spring Security + JWT
- 🗄️ PostgreSQL
- 🔄 Flyway (migrations)
- 🐳 Docker & Docker Compose
- 📦 Maven

---

## 📚 Documentação Completa

### Para Desenvolvedores Frontend

- **[API_DOCUMENTATION.md](API_DOCUMENTATION.md)** - Documentação completa de todos os endpoints e DTOs
- **[REACT_INTEGRATION_GUIDE.md](REACT_INTEGRATION_GUIDE.md)** - Guia completo de integração com React
- **[QUICK_REFERENCE.md](QUICK_REFERENCE.md)** - Referência rápida de endpoints
- **[CORS_CONFIGURATION.md](CORS_CONFIGURATION.md)** - Detalhes sobre configuração CORS
- **[CORS_SETUP_SUMMARY.md](CORS_SETUP_SUMMARY.md)** - Resumo das configurações CORS implementadas

### 🌐 CORS Habilitado

A API está configurada para aceitar requisições de **qualquer origem** durante o desenvolvimento:

```javascript
// Funciona direto do React sem problemas!
fetch('http://localhost:8080/runs')
  .then(res => res.json())
  .then(data => console.log(data));
```

⚠️ **Antes de produção:** Altere `CorsConfig.java` para aceitar apenas seu domínio!

---

## 📝 Notas Importantes

1. **Segurança**: Mude o `API_SECURITY_TOKEN_SECRET` em produção!
2. **CORS**: Em produção, configure apenas origens específicas no `CorsConfig.java`
3. **Autorização**: Usuários só podem editar/deletar suas próprias runs
4. **Formato de tempo**: Use "HH:MM" para runTime (ex: "2:30" = 2h30min)
5. **Flyway**: Migrations rodam automaticamente no startup

---

Desenvolvido com ❤️ e ☕

