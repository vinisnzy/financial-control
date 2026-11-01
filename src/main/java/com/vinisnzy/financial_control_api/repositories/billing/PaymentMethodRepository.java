package com.vinisnzy.financial_control_api.repositories.billing;

import com.vinisnzy.financial_control_api.entities.billing.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID> {
    Optional<PaymentMethod> findByIdAndUserId(UUID id, UUID userId);

    Page<PaymentMethod> findByUserId(UUID userId, Pageable pageable);

    Page<PaymentMethod> findByUserIdAndBankAccountId(UUID userId, UUID bankAccountId, Pageable pageable);
}
