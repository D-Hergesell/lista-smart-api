-- ===========================================================================
--  Lista Smart - Esquema relacional (PostgreSQL / NeonDB)
-- ===========================================================================
--  Compativel com PostgreSQL 14+. Pode ser aplicado manualmente na Neon
--  (psql / SQL Editor) ou deixado a cargo do Hibernate (ddl-auto=update).
--  Mantido aqui como documentacao canonica do modelo.
-- ===========================================================================

-- --------------------------------------------------------------------------
-- USERS - login simplificado (sem email, sem roles, sem recuperacao de senha)
-- --------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,            -- BCrypt, nunca texto puro
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- --------------------------------------------------------------------------
-- PRODUCTS / MARKETS - catalogo dos Spinners (dados publicos)
-- --------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS products (
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS markets (
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE
);

-- --------------------------------------------------------------------------
-- CONTRIBUTIONS - entidade principal (1 linha por item)
-- --------------------------------------------------------------------------
--  type = 'qr'     -> 1 linha por item extraido do cupom (points = 10 cada)
--  type = 'manual' -> 1 linha por envio (points = 5)
--  status = 'active' | 'deleted' (soft delete; pontos derivam dos 'active')
-- --------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS contributions (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id       BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type          VARCHAR(10)  NOT NULL CHECK (type IN ('qr', 'manual')),
    product       VARCHAR(120),
    market        VARCHAR(120),
    price         NUMERIC(10,2),
    purchase_date DATE,                              -- mapeado como "date" no JSON
    raw_data      TEXT,                              -- conteudo bruto do QR
    submitted_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    points        INTEGER      NOT NULL DEFAULT 0,
    status        VARCHAR(10)  NOT NULL DEFAULT 'active'
                               CHECK (status IN ('active', 'deleted'))
);

-- Indices para as consultas quentes (historico e ranking).
CREATE INDEX IF NOT EXISTS idx_contrib_user_status
    ON contributions (user_id, status);
CREATE INDEX IF NOT EXISTS idx_contrib_user_submitted
    ON contributions (user_id, submitted_at DESC);

-- --------------------------------------------------------------------------
-- NFCE_RESGATADA - anti-duplicidade de NFC-e (1 linha por chave de acesso)
-- --------------------------------------------------------------------------
--  Garante que uma mesma NFC-e (chave de 44 digitos) so gere pontos UMA vez no
--  sistema inteiro (escopo global). A chave persiste mesmo apos o soft-delete
--  das contribuicoes, de modo que reescanear o cupom nunca volta a pontuar.
-- --------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS nfce_resgatada (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    chave       VARCHAR(44)  NOT NULL UNIQUE,
    user_id     BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    redeemed_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
