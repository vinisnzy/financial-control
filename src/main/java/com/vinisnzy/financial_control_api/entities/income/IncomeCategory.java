package com.vinisnzy.financial_control_api.entities.income;

import com.vinisnzy.financial_control_api.entities.BaseEntity;
import com.vinisnzy.financial_control_api.entities.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "income_categories")
@Getter
@Setter
@NoArgsConstructor
public class IncomeCategory extends BaseEntity {
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String color;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "incomeCategory")
    private List<Income> incomes = new ArrayList<>();

    @OneToMany(mappedBy = "incomeCategory")
    private List<FixedIncome> fixedIncomes = new ArrayList<>();
}
