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

    @Column(nullable = false)
    private Boolean isDefault;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "incomeCategory")
    private List<Income> incomes = new ArrayList<>();

    @OneToMany(mappedBy = "incomeCategory")
    private List<FixedIncome> fixedIncomes = new ArrayList<>();
}
