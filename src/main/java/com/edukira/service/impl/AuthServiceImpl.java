package com.edukira.service.impl;

import com.edukira.dto.request.LoginRequest;
import com.edukira.dto.response.AuthResponse;
import com.edukira.entity.RefreshToken;
import com.edukira.entity.UserProfile;
import com.edukira.exception.EdukiraException;
import com.edukira.repository.RefreshTokenRepository;
import com.edukira.repository.UserProfileRepository;
import com.edukira.security.JwtService;
import com.edukira.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserProfileRepository userRepo;
    private final RefreshTokenRepository tokenRepo;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${edukira.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        UserProfile user = userRepo
                .findByEmailAndSchoolId(request.getEmail(), request.getSchoolId())
                .orElseThrow(() -> new EdukiraException("Email ou mot de passe incorrect", HttpStatus.UNAUTHORIZED));

        if (!user.getActive()) {
            throw new EdukiraException("Conta desactivada", HttpStatus.FORBIDDEN);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new EdukiraException("Email ou mot de passe incorrect", HttpStatus.UNAUTHORIZED);
        }

        // Revoke old tokens
        tokenRepo.revokeAllByUserId(user.getId());

        String accessToken = jwtService.generateAccessToken(
                user.getId(), user.getSchool().getId(), user.getRole().name());
        String rawRefresh = jwtService.generateRefreshToken(user.getId());

        tokenRepo.save(RefreshToken.builder()
                .user(user)
                .token(rawRefresh)
                .expiresAt(Instant.now().plusMillis(refreshExpirationMs))
                .build());

        // Update last login
        user.setLastLogin(Instant.now());
        userRepo.save(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefresh)
                .role(user.getRole().name())
                .preferredLanguage(user.getPreferredLanguage().name())
                .school(AuthResponse.SchoolSummary.builder()
                        .id(user.getSchool().getId().toString())
                        .name(user.getSchool().getName())
                        .country(user.getSchool().getCountry())
                        .type(user.getSchool().getType().name())
                        .build())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refresh(String refreshToken) {
        RefreshToken stored = tokenRepo.findByTokenAndRevokedFalse(refreshToken)
                .orElseThrow(() -> new EdukiraException("Refresh token inválido", HttpStatus.UNAUTHORIZED));

        if (stored.getExpiresAt().isBefore(Instant.now())) {
            stored.setRevoked(true);
            tokenRepo.save(stored);
            throw new EdukiraException("Refresh token expirado", HttpStatus.UNAUTHORIZED);
        }

        UUID userId = jwtService.extractUserId(refreshToken);
        UserProfile user = userRepo.findById(userId)
                .orElseThrow(() -> EdukiraException.notFound("Utilizador"));

        String newAccess = jwtService.generateAccessToken(
                user.getId(), user.getSchool().getId(), user.getRole().name());

        return AuthResponse.builder()
                .accessToken(newAccess)
                .refreshToken(refreshToken)
                .role(user.getRole().name())
                .preferredLanguage(user.getPreferredLanguage().name())
                .build();
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        tokenRepo.findByTokenAndRevokedFalse(refreshToken).ifPresent(t -> {
            t.setRevoked(true);
            tokenRepo.save(t);
        });
    }
}
