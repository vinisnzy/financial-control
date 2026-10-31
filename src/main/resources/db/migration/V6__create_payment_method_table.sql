CREATE
EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE payment_methods
(
    id              UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    name            TEXT        NOT NULL,
    type            TEXT        NOT NULL,
    bank_account_id UUID        NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT (now()),
    updated_at      TIMESTAMPTZ,
    user_id         UUID        NOT NULL,

    CONSTRAINT fk_payment_methods_bank_account FOREIGN KEY (bank_account_id) REFERENCES bank_accounts (id),
    CONSTRAINT fk_payment_methods_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_payment_methods_bank_account_id ON payment_methods (bank_account_id);
CREATE INDEX idx_payment_methods_user_id ON payment_methods (user_id);