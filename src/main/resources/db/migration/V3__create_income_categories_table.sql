CREATE
EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE income_categories
(
    id         UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    name       TEXT        NOT NULL,
    color      TEXT        NOT NULL,
    is_default BOOLEAN     NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT (now()),
    updated_at TIMESTAMPTZ,
    user_id    UUID,

    CONSTRAINT fk_income_categories_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT ck_income_categories_default_user CHECK (is_default = (user_id IS NULL))
);

CREATE INDEX idx_income_categories_user_id ON income_categories (user_id);