-- Seed do catalogo (idempotente). Para ativar, mude no application.properties:
--   spring.sql.init.mode=always
-- Os mesmos itens usados hoje no fallback local do app (MainActivity).
INSERT INTO products (name) VALUES
    ('Arroz'), ('Feijão'), ('Açúcar'), ('Café'), ('Óleo'),
    ('Leite'), ('Pão'), ('Carne'), ('Frango'), ('Ovos')
ON CONFLICT (name) DO NOTHING;

INSERT INTO markets (name) VALUES
    ('Carrefour'), ('Pão de Açúcar'), ('Extra'),
    ('Dia Supermercado'), ('Atacadão'), ('Assaí')
ON CONFLICT (name) DO NOTHING;
