package com.vinisnzy.financial_control_api.repositories.auth;

import com.vinisnzy.financial_control_api.TestcontainersConfig;
import com.vinisnzy.financial_control_api.config.JpaConfig;
import com.vinisnzy.financial_control_api.entities.auth.RefreshToken;
import com.vinisnzy.financial_control_api.repositories.utils.PersistUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfig.class, JpaConfig.class, PersistUtil.class})
class RefreshTokenRepositoryTest {

    private static final Instant NOW = Instant.parse("2026-06-01T12:00:00Z");

    @Autowired
    private RefreshTokenRepository repository;

    @Autowired
    private PersistUtil persistUtil;

    @Autowired
    private TestEntityManager em;

    @Test
    void revokeSession_shouldRevokeOnlyActiveTokensOfThatSession() {
        var user = persistUtil.persistUser("vini@test.com");
        var session = UUID.randomUUID();
        var previousRevocation = NOW.minusSeconds(3600);
        var active = persistUtil.persistRefreshToken(user, "active", session, NOW.plusSeconds(3600), null);
        var alreadyRevoked = persistUtil.persistRefreshToken(user, "revoked", session, NOW.plusSeconds(3600), previousRevocation);
        var otherSession = persistUtil.persistRefreshToken(user, "other", UUID.randomUUID(), NOW.plusSeconds(3600), null);

        repository.revokeSession(session, NOW);
        em.flush();
        em.clear();

        assertThat(revokedAt(active)).isEqualTo(NOW);
        assertThat(revokedAt(alreadyRevoked)).isEqualTo(previousRevocation);
        assertThat(revokedAt(otherSession)).isNull();
    }

    @Test
    void revokeSession_whenSessionHasNoTokens_shouldNotChangeAnything() {
        var user = persistUtil.persistUser("vini@test.com");
        var token = persistUtil.persistRefreshToken(user, "active", UUID.randomUUID(), NOW.plusSeconds(3600), null);

        repository.revokeSession(UUID.randomUUID(), NOW);
        em.flush();
        em.clear();

        assertThat(revokedAt(token)).isNull();
    }

    private Instant revokedAt(RefreshToken token) {
        return repository.findById(token.getId()).orElseThrow().getRevokedAt();
    }
}
