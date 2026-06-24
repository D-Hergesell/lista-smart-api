# Lista Smart — Backend (Spring Boot + NeonDB/PostgreSQL)

Backend REST do app Android.

- **Stack:** Java 17, Spring Boot 3.2, Spring Data JPA, Spring Security, JWT (HS256).
- **Banco:** NeonDB (PostgreSQL serverless), driver `org.postgresql`.

---

## 1. Esquema SQL (PostgreSQL)

Arquivo canônico: [`db/schema.sql`](db/schema.sql). Resumo:

| Tabela | Colunas principais |
|---|---|
| `users` | `id` (identity), `username` (único), `password_hash` (BCrypt), `created_at` |
| `products` | `id`, `name` (único) |
| `markets` | `id`, `name` (único) |
| `contributions` | `id`, `user_id`→users, `type` (`qr`\|`manual`), `product`, `market`, `price` `NUMERIC(10,2)`, `purchase_date`, `raw_data`, `submitted_at`, `points`, `status` (`active`\|`deleted`) |

Índices: `(user_id, status)` para somatórios e `(user_id, submitted_at DESC)` para o histórico.

> O Hibernate (`ddl-auto=update`) cria/ajusta o schema automaticamente; o
> `schema.sql` serve como documentação e para aplicação manual na Neon.

## 2. Diagrama ER (textual)

```
              ┌──────────────┐
              │    users     │
              │──────────────│
              │ id (PK)      │
              │ username  UQ │
              │ password_hash│
              │ created_at   │
              └──────┬───────┘
                     │ 1
                     │
                     │ N
              ┌──────┴────────────┐
              │  contributions    │
              │───────────────────│
              │ id (PK)           │
              │ user_id (FK)──────┼──→ users.id
              │ type qr|manual    │
              │ product / market  │
              │ price / date      │
              │ raw_data          │
              │ submitted_at      │
              │ points            │
              │ status active|del │
              └───────────────────┘

   products (id, name)      markets (id, name)     ← catálogo independente
   (sem FK; o app grava o NOME escolhido na contribuição, como já faz hoje)
```

`products`/`markets` alimentam os Spinners. A contribuição guarda o **nome**
(string) do produto/mercado, não uma FK — o app permite digitar "Outro".

## 3. Endpoints

| Método | Rota | Auth | Descrição |
|---|---|---|---|
| POST | `/auth/register` | pública | cria usuário, retorna token |
| POST | `/auth/login` | pública | autentica, retorna token |
| GET | `/users/me` | Bearer | perfil + selo + posição no ranking |
| POST | `/contributions` | Bearer | cria (qr → N itens; manual → 1) |
| GET | `/contributions/user/{id}` | Bearer (dono) | histórico (`submitted_at DESC`) |
| PUT | `/contributions/{id}` | Bearer (dono) | edita (não gera pontos) |
| DELETE | `/contributions/{id}` | Bearer (dono) | exclui (estorna pontos) |
| GET | `/products` | pública | catálogo de produtos |
| GET | `/markets` | pública | catálogo de mercados |
| GET | `/ranking` | Bearer | ranking global, dono marcado |

Respostas de erro: `{"error": "mensagem"}` com o status HTTP apropriado.

## 4. Fluxo de autenticação (simplificado)

1. `POST /auth/register` ou `/auth/login` com `{username, password}`.
2. Servidor valida, faz hash BCrypt (no register) e devolve
   `{token, userId, username}`.
3. App guarda o token (no `SessionManager`) e envia em todas as chamadas:
   `Authorization: Bearer <token>`.
4. JWT **stateless** (HS256, `sub`=userId): sem tabela de tokens nem sessão de
   servidor — a validação é feita pela assinatura a cada requisição.

Validações mínimas: `username` não vazio (≤ 50), senha ≥ 6 caracteres,
`username` único (409 em conflito).

## 5. Tabela de limiares por rank

Selo baseado em **pontos acumulados** (substitui o esquema antigo por contagem
de contribuições). Fonte: [`gamification/RankTable.java`](src/main/java/com/listasmart/api/gamification/RankTable.java).

| Rank | Pontos | Rank | Pontos | Rank | Pontos |
|---|---|---|---|---|---|
| Ferro IV | 0 | Ouro IV | 600 | Esmeralda IV | 1750 |
| Ferro III | 20 | Ouro III | 700 | Esmeralda III | 1970 |
| Ferro II | 45 | Ouro II | 810 | Esmeralda II | 2210 |
| Ferro I | 75 | Ouro I | 930 | Esmeralda I | 2470 |
| Bronze IV | 110 | Platina IV | 1070 | Diamante IV | 2760 |
| Bronze III | 150 | Platina III | 1220 | Diamante III | 3070 |
| Bronze II | 195 | Platina II | 1380 | Diamante II | 3400 |
| Bronze I | 245 | Platina I | 1550 | Diamante I | 3750 |
| Prata IV | 300 | | | Mestre | 4200 |
| Prata III | 365 | | | Grão-Mestre | 4800 |
| Prata II | 435 | | | Desafiante | 5500 |
| Prata I | 510 | | | | |

**Calibragem.** 1 manual = 5 pts, 1 item de QR = 10 pts. A curva é levemente
acelerada: cada faixa exige um pouco mais que a anterior.

`GET /users/me` retorna o selo atual, o próximo e o `progressPercent` dentro da
faixa (campo `badge`).

## 6. Regra de cálculo de pontos

- **Ganho no insert:** `qr` = **10 pts por item** extraído (1 contribuição por
  item); `manual` = **5 pts** por envio. Constantes em `ContributionService`.
- **Edição:** atualiza apenas os dados (`product`, `market`, `price`, `date`);
  **nunca** altera `points` nem `type`.
- **Exclusão = estorno:** **soft delete** (`status='deleted'`).

O total do usuário é sempre `SUM(points) WHERE status='active'` — não há coluna
de "saldo" denormalizado, então o soft delete estorna os pontos automaticamente
(a linha sai do somatório) e preserva o registro. O `SUM` por consulta é coberto
pelo índice `(user_id, status)`.

## 7. Estratégia de ranking global

Uma única consulta agrega todos os usuários
(`ContributionRepository.findRanking`):

```sql
SELECT u.id, u.username,
       COALESCE(SUM(CASE WHEN c.status='active' THEN c.points ELSE 0 END),0) AS points,
       COALESCE(SUM(CASE WHEN c.status='active' THEN 1 ELSE 0 END),0)        AS contributions
FROM users u LEFT JOIN contributions c ON c.user_id = u.id
GROUP BY u.id, u.username
ORDER BY points DESC, u.username ASC;
```

- `LEFT JOIN` inclui usuários ainda sem contribuições (zerados).
- O `GET /ranking` marca `currentUser=true` na linha do usuário autenticado.
- `GET /users/me` devolve a posição 1-based via `RankingService.positionOf`.
- Formato de cada item espelha o model Android `LeaderboardUser`
  (`name, points, contributions, avatar, currentUser`).

## 8. Boas práticas mínimas de segurança

- **Senha:** BCrypt (`BCryptPasswordEncoder`); nunca em texto puro; nunca
  retornada em nenhuma resposta.
- **Validação de entrada:** Bean Validation (`@NotBlank`, `@Size`) +
  checagens de regra no service (preço > 0, data não futura, tipo válido).
- **Autorização por `user_id`:** edição, exclusão e histórico só do próprio
  dono (403 caso contrário); o `user_id` vem do JWT, nunca do corpo da request.
- **Stateless / CSRF off:** API pura com token Bearer, sessão `STATELESS`.
- **Segredos fora do código:** connection string e `JWT_SECRET` por variável
  de ambiente.

## 9. Configuração NeonDB (`application.properties`)

Arquivo: [`src/main/resources/application.properties`](src/main/resources/application.properties).
Defina as variáveis de ambiente (a partir da connection string do painel Neon,
`postgresql://user:pass@host/db?sslmode=require`):

```properties
spring.datasource.url=jdbc:postgresql://SEU_HOST.neon.tech/listasmart?sslmode=require
spring.datasource.username=listasmart_owner
spring.datasource.password=********
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

```bash
# variáveis de ambiente (recomendado, não versionar segredos)
export NEON_DB_URL="jdbc:postgresql://SEU_HOST.neon.tech/listasmart?sslmode=require"
export NEON_DB_USER="listasmart_owner"
export NEON_DB_PASSWORD="********"
export JWT_SECRET="uma-chave-aleatoria-de-32-ou-mais-bytes"
```

---

## Como rodar

```bash
cd backend
mvn spring-boot:run
# API em http://localhost:8080
```

O catálogo (products/markets) é semeado automaticamente na 1ª subida
(`CatalogSeeder`) com os mesmos itens do fallback local do app.

## Integração no app Android

Trocar `ApiClient.BASE_URL` para a URL do backend e expandir `ApiService` com
os novos endpoints. Os DTOs usam os **mesmos nomes de campo** dos models
(`Contribution`, `Product`, `Market`, `LeaderboardUser`), incluindo `submittedAt`
(epoch ms) e `date` (`yyyy-MM-dd`), então a desserialização Gson não muda.
Adicionar um interceptor Retrofit com o header `Authorization: Bearer <token>`.
