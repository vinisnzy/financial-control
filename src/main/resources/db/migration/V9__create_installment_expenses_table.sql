CREATE
EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE installment_expenses
(
    id                  UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    name                TEXT           NOT NULL,
    total_amount        DECIMAL(14, 2) NOT NULL,
    installments        SMALLINT       NOT NULL,
    expense_category_id UUID           NOT NULL,
    payment_method_id   UUID           NOT NULL,
    first_due_date      DATE           NOT NULL,

    created_at          TIMESTAMPTZ    NOT NULL DEFAULT (now()),
    updated_at          TIMESTAMPTZ,
    user_id             UUID           NOT NULL,

    CONSTRAINT fk_installment_expenses_expense_category FOREIGN KEY (expense_category_id) REFERENCES expense_categories (id),
    CONSTRAINT fk_installment_expenses_payment_method FOREIGN KEY (payment_method_id) REFERENCES payment_methods (id),
    CONSTRAINT fk_installment_expenses_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_installment_expenses_expense_category_id on installment_expenses (expense_category_id);
CREATE INDEX idx_installment_expenses_payment_method_id on installment_expenses (payment_method_id);
CREATE INDEX idx_installment_expenses_user_id on installment_expenses (user_id);