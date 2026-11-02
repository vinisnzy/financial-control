CREATE
EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE expense_categories
(
    id         UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    name       TEXT        NOT NULL,
    color      TEXT        NOT NULL,
    is_default BOOLEAN     NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT (now()),
    updated_at TIMESTAMPTZ,
    user_id    UUID,

    CONSTRAINT fk_expense_categories_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT ck_expense_categories_default_user CHECK (is_default = (user_id IS NULL))
);

CREATE INDEX idx_expense_categories_user_id ON expense_categories (user_id);