package com.vinisnzy.financial_control_api.services.auth;

import com.vinisnzy.financial_control_api.config.auth.TokenProvider;
import com.vinisnzy.financial_control_api.dtos.auth.*;
import com.vinisnzy.financial_control_api.entities.auth.RefreshToken;
import com.vinisnzy.financial_control_api.entities.auth.User;
import com.vinisnzy.financial_control_api.exceptions.BusinessException;
import com.vinisnzy.financial_control_api.exceptions.ErrorCode;
import com.vinisnzy.financial_control_api.repositories.auth.RefreshTokenRepository;
import com.vinisnzy.financial_control_api.repositories.auth.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    @Value("${security.jwt.refresh-token.days}")
    private int refreshTokenExpiresInDays;

    public AuthService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository, PasswordEncoder passwordEncoder, TokenProvider tokenProvider, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with " + username));
    }

    public void signUp(SignUpRequest data) {
        if (this.userRepository.existsByEmail(data.email())) {
            throw new BusinessException(ErrorCode.USER_WITH_EMAIL_ALREADY_EXISTS,
                    "User with email '" + data.email() + "' already exists.");
        }
        String passwordHash = passwordEncoder.encode(data.password());
        User user = new User();
        user.setEmail(data.email());
        user.setPasswordHash(passwordHash);
        this.userRepository.save(user);
    }

    public SignInResponse signIn(SignInRequest data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());
        var authUser = authenticationManager.authenticate(usernamePassword);
        User user = (User) authUser.getPrincipal();
        return new SignInResponse(this.tokenProvider.generateToken(user), issueNewRefreshToken(user, UUID.randomUUID()));
    }

    public String issueNewRefreshToken(User user, UUID sessionId) {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setTokenHash(sha256(rawToken));
        refreshToken.setSessionId(sessionId);
        refreshToken.setExpiresAt(genRefreshExpirationDate());
        refreshToken.setUser(user);
        this.refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    @Transactional(noRollbackFor = BadCredentialsException.class)
    public SignInResponse refresh(RefreshRequest data) {
        RefreshToken old = this.refreshTokenRepository.findByTokenHash(sha256(data.refreshToken()))
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
        if (old.getRevokedAt() != null) {
            // Refresh token reuse, potential leak
            this.refreshTokenRepository.revokeSession(old.getSessionId(), Instant.now());
            throw new BadCredentialsException("Refresh token reused");
        }
        if (old.getExpiresAt().isBefore(Instant.now())) {
            throw new BadCredentialsException("Refresh token expired");
        }
        old.setRevokedAt(Instant.now());
        User user = old.getUser();
        return new SignInResponse(tokenProvider.generateToken(user), issueNewRefreshToken(user, old.getSessionId()));
    }

    @Transactional
    public void logout(LogoutRequest data) {
        this.refreshTokenRepository.findByTokenHash(sha256(data.refreshToken()))
                .ifPresent(rt -> this.refreshTokenRepository.revokeSession(rt.getSessionId(), Instant.now()));
    }

    private Instant genRefreshExpirationDate() {
        return Instant.now().plus(refreshTokenExpiresInDays, ChronoUnit.DAYS);
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
