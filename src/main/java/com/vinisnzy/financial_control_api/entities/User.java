package com.vinisnzy.financial_control_api.entities;

import com.vinisnzy.financial_control_api.entities.billing.BankAccount;
import com.vinisnzy.financial_control_api.entities.billing.PaymentMethod;
import com.vinisnzy.financial_control_api.entities.expense.Expense;
import com.vinisnzy.financial_control_api.entities.expense.ExpenseCategory;
import com.vinisnzy.financial_control_api.entities.expense.FixedExpense;
import com.vinisnzy.financial_control_api.entities.expense.InstallmentExpense;
import com.vinisnzy.financial_control_api.entities.income.FixedIncome;
import com.vinisnzy.financial_control_api.entities.income.Income;
import com.vinisnzy.financial_control_api.entities.income.IncomeCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User extends BaseEntity {
    @Column(unique = true, nullable = false, columnDefinition = "citext")
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @OneToMany(mappedBy = "user")
    private List<PaymentMethod> paymentMethods = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<BankAccount> bankAccounts = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Income> incomes = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<FixedIncome> fixedIncomes = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<IncomeCategory> incomeCategories = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Expense> expenses = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<FixedExpense> fixedExpenses = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<ExpenseCategory> expenseCategories = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<InstallmentExpense> installmentExpenses = new ArrayList<>();
}
