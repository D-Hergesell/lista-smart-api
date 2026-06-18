# Lista Smart API

Backend REST do app **Lista Smart** (Android). Spring Boot 3.2 + PostgreSQL
(NeonDB) + JWT. Substitui o placeholder MockAPI.

> Arquitetura, esquema SQL, regras de pontos, ranks e endpoints: ver
> [DESIGN.md](DESIGN.md).

## Stack
- Java 17, Spring Boot 3.2, Spring Data JPA, Spring Security
- PostgreSQL serverless (NeonDB), JWT HS256

## Variáveis de ambiente
| Variável | Descrição |
|---|---|
| `NEON_DB_URL` | `jdbc:postgresql://SEU_HOST.neon.tech/listasmart?sslmode=require` |
| `NEON_DB_USER` | usuário do banco (painel Neon) |
| `NEON_DB_PASSWORD` | senha do banco |
| `JWT_SECRET` | chave HS256, **≥ 32 caracteres** |
| `PORT` | opcional; default 8080 (o Render injeta automaticamente) |

## Rodar localmente

**Com Docker (recomendado, não precisa de Maven):**
```bash
docker build -t lista-smart-api .
docker run -p 8080:8080 \
  -e NEON_DB_URL="jdbc:postgresql://SEU_HOST.neon.tech/listasmart?sslmode=require" \
  -e NEON_DB_USER="..." -e NEON_DB_PASSWORD="..." \
  -e JWT_SECRET="uma-chave-aleatoria-de-32-ou-mais-bytes" \
  lista-smart-api
```

**Com Maven (se tiver instalado):**
```bash
export NEON_DB_URL=... NEON_DB_USER=... NEON_DB_PASSWORD=... JWT_SECRET=...
mvn spring-boot:run
```

API em `http://localhost:8080`. O catálogo é semeado na 1ª subida.

## Deploy no Render (via GitHub)
1. Crie o banco no [NeonDB](https://neon.tech) e copie a connection string.
2. Suba este projeto para um repositório no GitHub.
3. No [Render](https://render.com): **New → Blueprint** e aponte para o repo.
   O [`render.yaml`](render.yaml) cria o serviço Docker automaticamente.
4. Preencha `NEON_DB_URL`, `NEON_DB_USER`, `NEON_DB_PASSWORD` no painel
   (o `JWT_SECRET` é gerado pelo Render). Confirme que o `JWT_SECRET` tem
   ao menos 32 caracteres.
5. A URL pública gerada (`https://lista-smart-api.onrender.com`) vai em
   `ApiClient.BASE_URL` no app Android.

> Plano free do Render hiberna após inatividade: a 1ª chamada pode levar
> alguns segundos para "acordar" o serviço — normal para uso acadêmico.

## Smoke test
```bash
curl -X POST https://SUA_URL/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"teste","password":"123456"}'
# -> {"token":"...","userId":1,"username":"teste"}
```
