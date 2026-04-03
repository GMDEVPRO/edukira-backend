package com.edukira.service;

import com.edukira.dto.request.LoginRequest;
import com.edukira.dto.response.AuthResponse;
import com.edukira.entity.RefreshToken;
import com.edukira.entity.School;
import com.edukira.entity.UserProfile;
import com.edukira.enums.Language;
import com.edukira.enums.Role;
import com.edukira.exception.EdukiraException;
import com.edukira.repository.RefreshTokenRepository;
import com.edukira.repository.UserProfileRepository;
import com.edukira.security.JwtService;
import com.edukira.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService — testes unitários")
class AuthServiceTest {

    @Mock UserProfileRepository  userProfileRepo;
    @Mock RefreshTokenRepository refreshTokenRepo;
    @Mock JwtService             jwtService;
    @Mock PasswordEncoder        passwordEncoder;

    @InjectMocks AuthServiceImpl authService;

    private UserProfile user;
    private School      school;

    @BeforeEach
    void setUp() {
        school = School.builder()
                .id(UUID.randomUUID())
                .name("Lycée Test")
                .country("SN")
                .build();

        user = UserProfile.builder()
                .id(UUID.randomUUID())
                .school(school)
                .firstName("Amadou")
                .lastName("Diallo")
                .email("amadou@ecole.sn")
                .passwordHash("$2b$12$hashed")
                .role(Role.SCHOOL_ADMIN)
                .preferredLanguage(Language.fr)
                .active(true)
                .build();
    }

    // ── LOGIN ────────────────────────────────────────────────────────

    @Test
    @DisplayName("login com credenciais válidas retorna tokens")
    void login_validCredentials_returnsTokens() {
        LoginRequest req = new LoginRequest();
        req.setEmail("amadou@ecole.sn");
        req.setPassword("senha123");

        when(userProfileRepo.findByEmailAndSchoolId(eq("amadou@ecole.sn"), any()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha123", "$2b$12$hashed")).thenReturn(true);
        when(jwtService.generateToken(any(), any())).thenReturn("access.token.jwt");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh.token.jwt");
        when(refreshTokenRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        AuthResponse response = authService.login(req, school.getId());

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access.token.jwt");
        assertThat(response.getRefreshToken()).isEqualTo("refresh.token.jwt");
        assertThat(response.getRole()).isEqualTo(Role.SCHOOL_ADMIN);
        verify(refreshTokenRepo, times(1)).save(any());
    }

    @Test
    @DisplayName("login com password errada lança 401")
    void login_wrongPassword_throws401() {
        LoginRequest req = new LoginRequest();
        req.setEmail("amadou@ecole.sn");
        req.setPassword("errada");

        when(userProfileRepo.findByEmailAndSchoolId(eq("amadou@ecole.sn"), any()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("errada", "$2b$12$hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req, school.getId()))
                .isInstanceOf(EdukiraException.class)
                .hasMessageContaining("incorrect");
    }

    @Test
    @DisplayName("login com utilizador inactivo lança 403")
    void login_inactiveUser_throws403() {
        user.setActive(false);
        LoginRequest req = new LoginRequest();
        req.setEmail("amadou@ecole.sn");
        req.setPassword("senha123");

        when(userProfileRepo.findByEmailAndSchoolId(eq("amadou@ecole.sn"), any()))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(req, school.getId()))
                .isInstanceOf(EdukiraException.class);
    }

    @Test
    @DisplayName("login com email inexistente lança 401")
    void login_unknownEmail_throws401() {
        LoginRequest req = new LoginRequest();
        req.setEmail("naoexiste@ecole.sn");
        req.setPassword("senha123");

        when(userProfileRepo.findByEmailAndSchoolId(eq("naoexiste@ecole.sn"), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req, school.getId()))
                .isInstanceOf(EdukiraException.class);
    }

    // ── REFRESH TOKEN ────────────────────────────────────────────────

    @Test
    @DisplayName("refresh com token válido retorna novo access token")
    void refresh_validToken_returnsNewAccessToken() {
        RefreshToken rt = RefreshToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .token("refresh.token.jwt")
                .expiresAt(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        when(refreshTokenRepo.findByTokenAndRevokedFalse("refresh.token.jwt"))
                .thenReturn(Optional.of(rt));
        when(jwtService.generateToken(any(), any())).thenReturn("new.access.token");

        AuthResponse response = authService.refresh("refresh.token.jwt");

        assertThat(response.getAccessToken()).isEqualTo("new.access.token");
    }

    @Test
    @DisplayName("refresh com token expirado lança 401")
    void refresh_expiredToken_throws401() {
        RefreshToken rt = RefreshToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .token("expired.token")
                .expiresAt(Instant.now().minusSeconds(3600))
                .revoked(false)
                .build();

        when(refreshTokenRepo.findByTokenAndRevokedFalse("expired.token"))
                .thenReturn(Optional.of(rt));

        assertThatThrownBy(() -> authService.refresh("expired.token"))
                .isInstanceOf(EdukiraException.class);
    }

    @Test
    @DisplayName("refresh com token revogado lança 401")
    void refresh_revokedToken_throws401() {
        when(refreshTokenRepo.findByTokenAndRevokedFalse("revoked.token"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh("revoked.token"))
                .isInstanceOf(EdukiraException.class);
    }

    // ── LOGOUT ───────────────────────────────────────────────────────

    @Test
    @DisplayName("logout revoga o refresh token")
    void logout_revokesRefreshToken() {
        RefreshToken rt = RefreshToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .token("refresh.token.jwt")
                .revoked(false)
                .build();

        when(refreshTokenRepo.findByTokenAndRevokedFalse("refresh.token.jwt"))
                .thenReturn(Optional.of(rt));
        when(refreshTokenRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        authService.logout("refresh.token.jwt");

        assertThat(rt.isRevoked()).isTrue();
        verify(refreshTokenRepo, times(1)).save(rt);
    }
}
