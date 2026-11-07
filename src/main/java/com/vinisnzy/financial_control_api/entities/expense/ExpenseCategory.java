package com.vinisnzy.financial_control_api.entities.expense;

import com.vinisnzy.financial_control_api.entities.BaseEntity;
import com.vinisnzy.financial_control_api.entities.auth.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "expense_categories")
@Getter
@Setter
@NoArgsConstructor
public class ExpenseCategory extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false)
    private Boolean isDefault;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "expenseCategory")
    private List<Expense> expenses = new ArrayList<>();

    @OneToMany(mappedBy = "expenseCategory")
    private List<FixedExpense> fixedExpenses = new ArrayList<>();

    @OneToMany(mappedBy = "expenseCategory")
    private List<InstallmentExpense> installmentExpenses = new ArrayList<>();
}
