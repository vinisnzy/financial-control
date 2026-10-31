CREATE
EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE expense_categories
(
    id         UUID PRIMARY KEY                 DEFAULT gen_random_uuid(),
    name       TEXT        NOT NULL,
    color      TEXT        NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT (now()),
    updated_at TIMESTAMPTZ,
    user_id    UUID        NOT NULL,

    CONSTRAINT fk_expense_categories_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_expense_categories_user_id ON expense_categories (user_id);