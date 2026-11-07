package com.vinisnzy.financial_control_api.config.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.vinisnzy.financial_control_api.entities.auth.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Slf4j
@Service
public class TokenProvider {

    @Value("${security.jwt.token.secret_key}")
    private String jwtSecret;

    @Value("${security.jwt.access-token.minutes}")
    private int accessTokenExpiresInMinutes;

    public String generateToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
            return JWT.create().withSubject(user.getUsername()).withClaim("userId", user.getId().toString()).withExpiresAt(this.genAccessExpirationDate()).sign(algorithm);
        } catch (JWTCreationException e) {
            log.error("JWT creation exception", e);
            throw new JWTCreationException("Error while generating token", e);
        }
    }

    public Optional<String> validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
            return Optional.ofNullable(JWT.require(algorithm).build().verify(token).getSubject());
        } catch (JWTVerificationException e) {
            log.debug("Invalid JWT: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private Instant genAccessExpirationDate() {
        return Instant.now().plus(accessTokenExpiresInMinutes, ChronoUnit.MINUTES);
    }
}
