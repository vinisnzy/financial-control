package com.vinisnzy.financial_control_api.services.auth;

import com.vinisnzy.financial_control_api.config.auth.TokenProvider;
import com.vinisnzy.financial_control_api.dtos.auth.LogoutRequest;
import com.vinisnzy.financial_control_api.dtos.auth.RefreshRequest;
import com.vinisnzy.financial_control_api.dtos.auth.SignInRequest;
import com.vinisnzy.financial_control_api.dtos.auth.SignUpRequest;
import com.vinisnzy.financial_control_api.entities.auth.RefreshToken;
import com.vinisnzy.financial_control_api.entities.auth.User;
import com.vinisnzy.financial_control_api.exceptions.BusinessException;
import com.vinisnzy.financial_control_api.exceptions.ErrorCode;
import com.vinisnzy.financial_control_api.repositories.auth.RefreshTokenRepository;
import com.vinisnzy.financial_control_api.repositories.auth.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    RefreshTokenRepository refreshTokenRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    TokenProvider tokenProvider;
    @Mock
    AuthenticationManager authenticationManager;

    @InjectMocks
    AuthService service;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "refreshTokenExpiresInDays", 7);

        user = new User();
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.setEmail("vini@test.com");
        user.setPasswordHash("hashed");
    }

    private static String sha256(String value) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest);
    }

    private RefreshToken refreshToken(Instant expiresAt, Instant revokedAt, UUID sessionId) {
        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setSessionId(sessionId);
        rt.setExpiresAt(expiresAt);
        rt.setRevokedAt(revokedAt);
        return rt;
    }

    @Test
    void loadUserByUsername_shouldReturnUser() {
        when(userRepository.findByEmail("vini@test.com")).thenReturn(Optional.of(user));

        assertThat(service.loadUserByUsername("vini@test.com")).isSameAs(user);
    }

    @Test
    void loadUserByUsername_whenNotFound_shouldThrow() {
        when(userRepository.findByEmail("x@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("x@test.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void signUp_shouldSaveUserWithEncodedPassword() {
        when(userRepository.existsByEmail("vini@test.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("encoded");

        service.signUp(new SignUpRequest("vini@test.com", "123456"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("vini@test.com");
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("encoded");
    }

    @Test
    void signUp_whenEmailExists_shouldThrowBusinessException() {
        when(userRepository.existsByEmail("vini@test.com")).thenReturn(true);

        assertThatThrownBy(() -> service.signUp(new SignUpRequest("vini@test.com", "123456")))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(ErrorCode.USER_WITH_EMAIL_ALREADY_EXISTS);

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void signIn_shouldReturnAccessAndRefreshToken() {
        var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(tokenProvider.generateToken(user)).thenReturn("jwt-token");

        var response = service.signIn(new SignInRequest("vini@test.com", "123456"));

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.refreshToken()).isNotBlank();

        ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(authCaptor.capture());
        assertThat(authCaptor.getValue().getPrincipal()).isEqualTo("vini@test.com");
        assertThat(authCaptor.getValue().getCredentials()).isEqualTo("123456");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void signIn_withBadCredentials_shouldPropagateAndNotIssueTokens() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> service.signIn(new SignInRequest("vini@test.com", "wrong")))
                .isInstanceOf(BadCredentialsException.class);

        verifyNoInteractions(tokenProvider, refreshTokenRepository);
    }

    @Test
    void issueNewRefreshToken_shouldPersistHashNotRawToken() throws Exception {
        UUID sessionId = UUID.randomUUID();

        String raw = service.issueNewRefreshToken(user, sessionId);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        RefreshToken saved = captor.getValue();

        assertThat(saved.getTokenHash()).isEqualTo(sha256(raw)).isNotEqualTo(raw);
        assertThat(saved.getSessionId()).isEqualTo(sessionId);
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getRevokedAt()).isNull();
        assertThat(saved.getExpiresAt())
                .isCloseTo(Instant.now().plus(7, ChronoUnit.DAYS), within(5, ChronoUnit.SECONDS));
    }

    @Test
    void issueNewRefreshToken_shouldGenerateDifferentTokens() {
        String a = service.issueNewRefreshToken(user, UUID.randomUUID());
        String b = service.issueNewRefreshToken(user, UUID.randomUUID());

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void refresh_validToken_shouldRevokeOldAndIssueNewInSameSession() throws Exception {
        UUID sessionId = UUID.randomUUID();
        RefreshToken old = refreshToken(Instant.now().plus(1, ChronoUnit.DAYS), null, sessionId);
        when(refreshTokenRepository.findByTokenHash(sha256("raw-old"))).thenReturn(Optional.of(old));
        when(tokenProvider.generateToken(user)).thenReturn("new-jwt");

        var response = service.refresh(new RefreshRequest("raw-old"));

        assertThat(response.accessToken()).isEqualTo("new-jwt");
        assertThat(response.refreshToken()).isNotEqualTo("raw-old");
        assertThat(old.getRevokedAt()).isNotNull();

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getSessionId()).isEqualTo(sessionId);
        assertThat(captor.getValue().getTokenHash()).isEqualTo(sha256(response.refreshToken()));
    }

    @Test
    void refresh_unknownToken_shouldThrow() {
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.refresh(new RefreshRequest("nope")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid refresh token");

        verifyNoInteractions(tokenProvider);
    }

    @Test
    void refresh_reusedToken_shouldRevokeWholeSession() {
        UUID sessionId = UUID.randomUUID();
        RefreshToken old = refreshToken(Instant.now().plus(1, ChronoUnit.DAYS), Instant.now().minusSeconds(60), sessionId);
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(old));

        assertThatThrownBy(() -> service.refresh(new RefreshRequest("raw")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Refresh token reused");

        verify(refreshTokenRepository).revokeSession(eq(sessionId), any(Instant.class));
        verify(refreshTokenRepository, never()).save(any());
        verifyNoInteractions(tokenProvider);
    }

    @Test
    void refresh_expiredToken_shouldThrowAndNotRevoke() {
        RefreshToken old = refreshToken(Instant.now().minusSeconds(1), null, UUID.randomUUID());
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(old));

        assertThatThrownBy(() -> service.refresh(new RefreshRequest("raw")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Refresh token expired");

        assertThat(old.getRevokedAt()).isNull();
        verify(refreshTokenRepository, never()).save(any());
        verify(refreshTokenRepository, never()).revokeSession(any(), any());
    }

    @Test
    void logout_existingToken_shouldRevokeSession() throws Exception {
        UUID sessionId = UUID.randomUUID();
        RefreshToken rt = refreshToken(Instant.now().plus(1, ChronoUnit.DAYS), null, sessionId);
        when(refreshTokenRepository.findByTokenHash(sha256("raw"))).thenReturn(Optional.of(rt));

        service.logout(new LogoutRequest("raw"));

        verify(refreshTokenRepository).revokeSession(eq(sessionId), any(Instant.class));
    }

    @Test
    void logout_unknownToken_shouldDoNothing() {
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        service.logout(new LogoutRequest("raw"));

        verify(refreshTokenRepository, never()).revokeSession(any(), any());
    }
}
