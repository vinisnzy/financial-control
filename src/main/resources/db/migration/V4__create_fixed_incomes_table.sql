CREATE
EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE fixed_incomes
(
    id                 UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    name               TEXT           NOT NULL,
    amount             DECIMAL(14, 2) NOT NULL,
    income_category_id UUID           NOT NULL,
    bank_account_id    UUID           NOT NULL,
    payday             SMALLINT       NOT NULL,
    start_date         DATE           NOT NULL,
    end_date           DATE,

    created_at         TIMESTAMPTZ    NOT NULL DEFAULT (now()),
    updated_at         TIMESTAMPTZ,
    user_id            UUID           NOT NULL,

    CONSTRAINT fk_fixed_incomes_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_fixed_incomes_category FOREIGN KEY (income_category_id) REFERENCES income_categories (id),
    CONSTRAINT fk_fixed_incomes_bank_account FOREIGN KEY (bank_account_id) REFERENCES bank_accounts (id)
);

CREATE INDEX idx_fixed_incomes_user_id ON fixed_incomes (user_id);
CREATE INDEX idx_fixed_incomes_income_category_id ON fixed_incomes (income_category_id);
CREATE INDEX idx_fixed_incomes_bank_account_id ON fixed_incomes (bank_account_id);