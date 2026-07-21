package com.abcbank.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Issues and validates HMAC-signed JWT access tokens.
 *
 * <p>This is infrastructure (not business logic) and is therefore fully
 * implemented so that the security specification ({@code SecurityTest}) can be
 * exercised.
 */
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMillis;

    public JwtService(
            @Value("${security.jwt.secret-key}") String secretKey,
            @Value("${security.jwt.expiration}") long expirationMillis) {
        this.signingKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expirationMillis;
    }

    /**
     * Generate a signed token whose subject is the supplied username (email).
     */
    public String generateToken(String username) {
        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime() + expirationMillis);
        return Jwts.builder()
                .subject(username)
                .issuedAt(issuedAt)
                .expiration(expiresAt)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Extract the subject (username) from a token.
     *
     * @throws JwtException if the token is malformed, tampered with, or expired
     */
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * @return {@code true} only if the token is well-formed, correctly signed,
     * not expired, and its subject matches {@code username}.
     */
    public boolean isTokenValid(String token, String username) {
        try {
            Claims claims = parseClaims(token);
            return username.equals(claims.getSubject()) && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
