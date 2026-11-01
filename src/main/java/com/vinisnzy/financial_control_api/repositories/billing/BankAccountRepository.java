package com.vinisnzy.financial_control_api.repositories.billing;

import com.vinisnzy.financial_control_api.entities.billing.BankAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BankAccountRepository extends JpaRepository<BankAccount, UUID> {
    Optional<BankAccount> findByIdAndUserId(UUID id, UUID userId);

    Page<BankAccount> findByUserId(UUID userId, Pageable pageable);
}
