# Lista Smart API

Backend REST do app Lista Smart. Modelagem de dados, regras de pontos/ranks e
notas de design: [DESIGN.md](DESIGN.md).

> Trabalho acadêmico interdisciplinar da UNESC, das disciplinas de Engenharia de
> Software e Desenvolvimento para Dispositivos Móveis.

## Stack
Java 17 · Spring Boot 3.2 (Web, Data JPA, Security, Validation) · PostgreSQL
(NeonDB) · JWT HS256 (jjwt) · Maven

## Estrutura
```
com.listasmart.api
├── controller/   Auth, Catalog, Contribution, Ranking, User, DevNfce
├── service/      Auth, Contribution, Ranking, Avatar
├── gamification/ GamificationService, RankTable (selo por pontos acumulados)
├── nfce/         NfceKey (chave + módulo 11), NfceItemResolver/MockNfceResolver, NfceNota
├── entity/       User, Product, Market, Contribution, NfceResgatada
├── repository/   *Repository (Spring Data JPA) + projection/RankingRow
├── dto/          Auth, Catalog, Contribution(Request/Response), Leaderboard, RankProgress, UserMe
├── security/     JwtService, JwtAuthFilter, SecurityConfig, CurrentUser
├── exception/    ApiException, GlobalExceptionHandler
└── config/       CatalogSeeder (semeia products/markets na 1ª subida)
```

## Endpoints
| Método | Rota | Auth | Descrição |
|---|---|---|---|
| POST | `/auth/register` | pública | cria usuário, retorna `{token,userId,username}` |
| POST | `/auth/login` | pública | autentica, retorna token |
| GET | `/users/me` | Bearer | perfil + pontos + selo + posição no ranking |
| POST | `/contributions` | Bearer | cria contribuição (qr → N itens; manual → 1) |
| GET | `/contributions/user/{id}` | Bearer (dono) | histórico (`submitted_at DESC`) |
| PUT | `/contributions/{id}` | Bearer (dono) | edita (não altera pontos/tipo) |
| DELETE | `/contributions/{id}` | Bearer (dono) | soft-delete (estorna pontos) |
| GET | `/products` · `/markets` | pública | catálogo (spinners do app) |
| GET | `/ranking` | Bearer | ranking global, dono marcado |
| GET | `/dev/nfce/key` · `/dev/nfce/qr` | dev | gera chave/QR de NFC-e de teste |

`/dev/nfce/*` só existe com `app.dev-tools.enabled=true`. Erros saem como
`{"error":"mensagem"}` com o status HTTP correspondente (`GlobalExceptionHandler`).

## Segurança
- JWT HS256 stateless (`sub` = userId); filtro `JwtAuthFilter` + `SecurityConfig`
  (sessão `STATELESS`, CSRF off).
- Senhas em BCrypt; nunca retornadas.
- Autorização por dono: edição/exclusão/histórico exigem o `user_id` do token.

## Variáveis de ambiente
| Variável | Descrição |
|---|---|
| `NEON_DB_URL` | `jdbc:postgresql://HOST.neon.tech/listasmart?sslmode=require` |
| `NEON_DB_USER` / `NEON_DB_PASSWORD` | credenciais do banco |
| `JWT_SECRET` | chave HS256, ≥ 32 caracteres |
| `PORT` | opcional; default 8080 |

## Run
**Docker:**
```bash
docker build -t lista-smart-api .
docker run -p 8080:8080 \
  -e NEON_DB_URL="jdbc:postgresql://HOST.neon.tech/listasmart?sslmode=require" \
  -e NEON_DB_USER="..." -e NEON_DB_PASSWORD="..." \
  -e JWT_SECRET="<chave de 32+ caracteres>" \
  lista-smart-api
```
**Maven:**
```bash
export NEON_DB_URL=... NEON_DB_USER=... NEON_DB_PASSWORD=... JWT_SECRET=...
mvn spring-boot:run
```
`ddl-auto=update` cria/ajusta o schema; o catálogo é semeado na 1ª subida
(`CatalogSeeder`). API em `http://localhost:8080`.

## Deploy (Render)
`render.yaml` provisiona o serviço Docker via Blueprint. Definir `NEON_DB_URL`,
`NEON_DB_USER`, `NEON_DB_PASSWORD` no painel (`JWT_SECRET` gerado pelo Render).
A URL pública vai em `API_BASE_URL` no app.

## Testes
```bash
mvn test
```
JUnit 5 + Mockito (sem banco):
- `NfceKeyTest` — chave NFC-e: 44 dígitos, dígito verificador (módulo 11), parse da URL SEFAZ.
- `GamificationServiceTest` — selo/rank e progresso por pontos.
- `ContributionServiceTest` — pontos (5 manual / 10 por item QR), anti-duplicidade, validações.

## Smoke test
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"teste","password":"123456"}'
# -> {"token":"...","userId":1,"username":"teste"}
```
