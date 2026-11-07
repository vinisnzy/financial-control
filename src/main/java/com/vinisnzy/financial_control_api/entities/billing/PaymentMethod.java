package com.vinisnzy.financial_control_api.entities.billing;

import com.vinisnzy.financial_control_api.entities.BaseEntity;
import com.vinisnzy.financial_control_api.entities.auth.User;
import com.vinisnzy.financial_control_api.entities.expense.Expense;
import com.vinisnzy.financial_control_api.entities.expense.FixedExpense;
import com.vinisnzy.financial_control_api.entities.expense.InstallmentExpense;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payment_methods")
@Getter
@Setter
@NoArgsConstructor
public class PaymentMethod extends BaseEntity {
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bank_account_id", nullable = false)
    private BankAccount bankAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "paymentMethod")
    private List<Expense> expenses = new ArrayList<>();

    @OneToMany(mappedBy = "paymentMethod")
    private List<FixedExpense> fixedExpenses = new ArrayList<>();

    @OneToMany(mappedBy = "paymentMethod")
    private List<InstallmentExpense> installmentExpenses = new ArrayList<>();
}
