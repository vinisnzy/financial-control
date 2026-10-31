CREATE
EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE bank_accounts
(
    id              UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    name            TEXT           NOT NULL,
    initial_balance DECIMAL(14, 2) NOT NULL,

    created_at      TIMESTAMPTZ    NOT NULL DEFAULT (now()),
    updated_at      TIMESTAMPTZ,
    user_id         UUID           NOT NULL,

    CONSTRAINT fk_bank_accounts_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_bank_accounts_user_id ON bank_accounts (user_id);