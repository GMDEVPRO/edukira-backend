package com.edukira.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class JwtService {

    @Value("${edukira.jwt.secret}")
    private String secret;

    @Value("${edukira.jwt.expiration-ms}")
    private long expirationMs;

    @Value("${edukira.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /** Token para staff da escola (ADMIN, TEACHER, etc.) */
    public String generateAccessToken(UUID userId, UUID schoolId, String role) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .addClaims(Map.of(
                        "schoolId", schoolId.toString(),
                        "role", role,
                        "tokenType", "STAFF"))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** Token para aluno — contém studentAccountId + schoolId */
    public String generateStudentAccessToken(UUID studentAccountId, UUID schoolId, UUID studentId) {
        return Jwts.builder()
                .setSubject(studentAccountId.toString())
                .addClaims(Map.of(
                        "schoolId", schoolId.toString(),
                        "studentId", studentId != null ? studentId.toString() : "",
                        "role", "STUDENT",
                        "tokenType", "STUDENT"))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(UUID userId) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractAllClaims(token).getSubject());
    }

    public UUID extractSchoolId(String token) {
        String sid = extractAllClaims(token).get("schoolId", String.class);
        return (sid == null || sid.isEmpty()) ? null : UUID.fromString(sid);
    }

    public UUID extractStudentId(String token) {
        String sid = extractAllClaims(token).get("studentId", String.class);
        return (sid == null || sid.isEmpty()) ? null : UUID.fromString(sid);
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public String extractTokenType(String token) {
        return extractAllClaims(token).get("tokenType", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token inválido: {}", e.getMessage());
            return false;
        }
    }
}
