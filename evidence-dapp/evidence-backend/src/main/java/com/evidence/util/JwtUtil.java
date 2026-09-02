package com.evidence.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expiration}")
    private Long accessExpiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    public String generateAccessToken(Long userId, String username) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + accessExpiration);
        return JWT.create()
                .withClaim("userId", userId)
                .withClaim("username", username)
                .withClaim("type", "access")
                .withIssuedAt(now)
                .withExpiresAt(expireDate)
                .withJWTId(UUID.randomUUID().toString())
                .sign(Algorithm.HMAC256(secret));
    }

    public String generateRefreshToken(Long userId, String username) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + refreshExpiration);
        return JWT.create()
                .withClaim("userId", userId)
                .withClaim("username", username)
                .withClaim("type", "refresh")
                .withIssuedAt(now)
                .withExpiresAt(expireDate)
                .withJWTId(UUID.randomUUID().toString())
                .sign(Algorithm.HMAC256(secret));
    }

    public DecodedJWT parseToken(String token) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret)).build();
        return verifier.verify(token);
    }

    public boolean isTokenExpired(String token) {
        try {
            DecodedJWT jwt = parseToken(token);
            return jwt.getExpiresAt().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public boolean isAccessToken(String token) {
        try {
            return "access".equals(parseToken(token).getClaim("type").asString());
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            return "refresh".equals(parseToken(token).getClaim("type").asString());
        } catch (Exception e) {
            return false;
        }
    }

    public Long getAccessExpirationSeconds() { return accessExpiration / 1000; }
    public Long getRefreshExpirationSeconds() { return refreshExpiration / 1000; }
}
