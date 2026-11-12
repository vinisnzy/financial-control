package com.vinisnzy.financial_control_api.utils;

import com.vinisnzy.financial_control_api.entities.auth.RefreshToken;
import com.vinisnzy.financial_control_api.entities.auth.User;
import com.vinisnzy.financial_control_api.entities.billing.BankAccount;
import com.vinisnzy.financial_control_api.entities.billing.PaymentMethod;
import com.vinisnzy.financial_control_api.entities.expense.ExpenseCategory;
import com.vinisnzy.financial_control_api.entities.expense.FixedExpense;
import com.vinisnzy.financial_control_api.entities.income.FixedIncome;
import com.vinisnzy.financial_control_api.entities.income.IncomeCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class PersistUtil {

    @Autowired
    private TestEntityManager em;

    public User persistUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("password");
        return em.persist(user);
    }

    public IncomeCategory persistIncomeCategory(String name, User user, boolean isDefault) {
        IncomeCategory category = new IncomeCategory();
        category.setName(name);
        category.setUser(user);
        category.setIsDefault(isDefault);
        category.setColor("#FFFFFF");
        return em.persist(category);
    }

    public ExpenseCategory persistExpenseCategory(String name, User user, boolean isDefault) {
        ExpenseCategory category = new ExpenseCategory();
        category.setName(name);
        category.setColor("#FFFFFF");
        category.setUser(user);
        category.setIsDefault(isDefault);
        return em.persist(category);
    }

    public BankAccount persistBankAccount(String name, User user) {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setName(name);
        bankAccount.setInitialBalance(new BigDecimal(0));
        bankAccount.setUser(user);
        return em.persist(bankAccount);
    }

    public PaymentMethod persistPaymentMethod(String name, User user) {
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setName(name);
        paymentMethod.setType("type");
        paymentMethod.setUser(user);
        paymentMethod.setBankAccount(persistBankAccount(name, user));
        return em.persist(paymentMethod);
    }

    public FixedExpense persistFixedExpense(User user, String name, LocalDate startDate, LocalDate endDate) {
        FixedExpense fixedExpense = new FixedExpense();
        fixedExpense.setUser(user);
        fixedExpense.setName(name);
        fixedExpense.setAmount(new BigDecimal(0));
        fixedExpense.setExpenseCategory(persistExpenseCategory(name, user, false));
        fixedExpense.setPaymentMethod(persistPaymentMethod(name, user));
        fixedExpense.setDueDay((short) 5);
        fixedExpense.setStartDate(startDate);
        fixedExpense.setEndDate(endDate);
        return em.persist(fixedExpense);
    }

    public FixedIncome persistFixedIncome(User user, String name, LocalDate startDate, LocalDate endDate) {
        FixedIncome fixedIncome = new FixedIncome();
        fixedIncome.setUser(user);
        fixedIncome.setName(name);
        fixedIncome.setAmount(new BigDecimal(0));
        fixedIncome.setIncomeCategory(persistIncomeCategory(name, user, false));
        fixedIncome.setBankAccount(persistBankAccount(name, user));
        fixedIncome.setPayday((short) 5);
        fixedIncome.setStartDate(startDate);
        fixedIncome.setEndDate(endDate);
        return em.persist(fixedIncome);
    }

    public RefreshToken persistRefreshToken(User user, String tokenHash, UUID sessionId, Instant expiresAt, Instant revokedAt) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setSessionId(sessionId);
        refreshToken.setExpiresAt(expiresAt);
        refreshToken.setRevokedAt(revokedAt);
        return em.persist(refreshToken);
    }
}
