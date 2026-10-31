CREATE
EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE income_categories
(
    id         UUID PRIMARY KEY                 DEFAULT gen_random_uuid(),
    name       TEXT        NOT NULL,
    color      TEXT        NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT (now()),
    updated_at TIMESTAMPTZ,
    user_id    UUID        NOT NULL,

    CONSTRAINT fk_income_categories_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_income_categories_user_id ON income_categories (user_id);