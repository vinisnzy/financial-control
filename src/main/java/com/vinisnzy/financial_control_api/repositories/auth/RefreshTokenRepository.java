package com.vinisnzy.financial_control_api.repositories.auth;

import com.vinisnzy.financial_control_api.entities.auth.RefreshToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Transactional
    @Modifying(flushAutomatically = true)
    @Query("UPDATE RefreshToken t SET t.revokedAt = :now WHERE t.sessionId = :sessionId AND t.revokedAt IS NULL")
    void revokeSession(UUID sessionId, Instant now);

    @Transactional
    @Modifying
    void deleteByExpiresAtBefore(Instant limit);
}
