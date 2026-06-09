package com.university.fintech.wire.security.impl;

import com.university.fintech.domain.dto.UserDTO;
import com.university.fintech.wire.security.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil implements JwtProvider {

    private static final int MIN_SECRET_BYTES = 32;

    private final Key key;
    private final long jwtExpirationMs;

    // Set up secret key
    public JwtUtil(
            // Collect secret key from configuration file to sign JWT
            @Value("${application.jwt.secret:}") String jwtSecret,
            @Value("${application.jwt.expiration-ms:}") long jwtExpirationMs
    ) {
        // Convert secret to byte[]
        byte[] secretBytes = jwtSecret == null ? new byte[0] : jwtSecret.getBytes(StandardCharsets.UTF_8);
        // SHA256 standard needs more than 32 bytes for the secret key
        if (secretBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("app.jwt.secret must be at least 32 bytes for HS256");
        }
        // Create key to sign & verify JWT
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.jwtExpirationMs = jwtExpirationMs;
    }

    // Create JWT
    @Override
    public String generateToken(UserDTO userDTO) {
        // Save user information into token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userDTO.getId().toString());
        claims.put("fullName", userDTO.getFullName());
        claims.put("roles", new ArrayList<>(userDTO.getRoleCodes()));
        claims.put("permissions", new ArrayList<>(userDTO.getPermissionCodes()));
        // Collect first role (to instant use such as authorization)
        userDTO.getRoleCodes().stream().findFirst().ifPresent(role -> claims.put("role", role));

        // Build token
        return Jwts.builder()
                // payload
                .setClaims(claims)
                // subject = email
                .setSubject(userDTO.getEmail())
                // created at
                .setIssuedAt(new Date(System.currentTimeMillis()))
                // expiration time
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                // sign token with key and algorithm
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Collect email from token
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Parse JWT and verify signature then return payload (if token is changed -> throw exception)
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public long getRemainingValidityMillis(String token) {
        Date expiration = extractAllClaims(token).getExpiration();
        return Math.max(0, expiration.getTime() - System.currentTimeMillis());
    }
}
