package com.edukira.service.impl;

import com.edukira.dto.request.StudentLoginRequest;
import com.edukira.dto.request.StudentRegisterRequest;
import com.edukira.dto.response.StudentAuthResponse;
import com.edukira.entity.RefreshToken;
import com.edukira.entity.School;
import com.edukira.entity.StudentAccount;
import com.edukira.enums.StudentAccountStatus;
import com.edukira.exception.EdukiraException;
import com.edukira.repository.RefreshTokenRepository;
import com.edukira.repository.SchoolRepository;
import com.edukira.repository.StudentAccountRepository;
import com.edukira.security.JwtService;
import com.edukira.service.StudentAuthService;
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
public class StudentAuthServiceImpl implements StudentAuthService {

    private final StudentAccountRepository accountRepo;
    private final SchoolRepository schoolRepo;
    private final RefreshTokenRepository tokenRepo;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${edukira.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Override
    @Transactional
    public StudentAuthResponse register(StudentRegisterRequest req) {
        School school = schoolRepo.findById(req.getSchoolId())
                .orElseThrow(() -> EdukiraException.notFound("Escola"));

        // Verifica se já existe conta com mesmo documento nesta escola
        if (accountRepo.findByDocumentNumberAndSchoolId(req.getDocumentNumber(), req.getSchoolId()).isPresent()) {
            throw EdukiraException.badRequest("Já existe uma conta com este número de documento nesta escola");
        }

        // Verifica email duplicado
        if (req.getEmail() != null && accountRepo.findByEmail(req.getEmail()).isPresent()) {
            throw EdukiraException.badRequest("Este email já está em uso");
        }

        StudentAccount account = StudentAccount.builder()
                .school(school)
                .fullName(req.getFullName())
                .documentNumber(req.getDocumentNumber())
                .documentType(req.getDocumentType())
                .email(req.getEmail())
                .phone(req.getPhone())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .preferredLanguage(req.getPreferredLanguage() != null ? req.getPreferredLanguage()
                        : school.getDefaultLanguage())
                .status(StudentAccountStatus.PENDING_APPROVAL)
                .build();

        account = accountRepo.save(account);
        log.info("Nova conta de aluno criada: {} — aguarda aprovação da escola {}", account.getId(), school.getName());

        // Retorna token mesmo em PENDING — frontend mostra mensagem de aguardar aprovação
        String accessToken = jwtService.generateStudentAccessToken(account.getId(), school.getId(), null);
        String refreshToken = jwtService.generateRefreshToken(account.getId());

        tokenRepo.save(RefreshToken.builder()
                .user(null) // student account usa tabela separada logicamente
                .token(refreshToken)
                .expiresAt(Instant.now().plusMillis(refreshExpirationMs))
                .build());

        return buildAuthResponse(account, accessToken, refreshToken);
    }

    @Override
    @Transactional
    public StudentAuthResponse login(StudentLoginRequest req) {
        StudentAccount account = accountRepo
                .findByDocumentNumberAndSchoolId(req.getDocumentNumber(), req.getSchoolId())
                .orElseThrow(() -> new EdukiraException("Documento ou password incorretos", HttpStatus.UNAUTHORIZED));

        if (account.getStatus() == StudentAccountStatus.REJECTED) {
            throw new EdukiraException(
                "Conta rejeitada pela escola. Motivo: " + account.getRejectionReason(),
                HttpStatus.FORBIDDEN);
        }

        if (account.getStatus() == StudentAccountStatus.SUSPENDED) {
            throw new EdukiraException("Conta suspensa. Contacte a escola.", HttpStatus.FORBIDDEN);
        }

        if (!passwordEncoder.matches(req.getPassword(), account.getPasswordHash())) {
            throw new EdukiraException("Documento ou password incorretos", HttpStatus.UNAUTHORIZED);
        }

        tokenRepo.revokeAllByUserId(account.getId());

        UUID linkedStudentId = account.getStudent() != null ? account.getStudent().getId() : null;
        String accessToken = jwtService.generateStudentAccessToken(
                account.getId(), account.getSchool().getId(), linkedStudentId);
        String refreshToken = jwtService.generateRefreshToken(account.getId());

        tokenRepo.save(RefreshToken.builder()
                .token(refreshToken)
                .expiresAt(Instant.now().plusMillis(refreshExpirationMs))
                .build());

        account.setLastLogin(Instant.now());
        accountRepo.save(account);

        return buildAuthResponse(account, accessToken, refreshToken);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        tokenRepo.findByTokenAndRevokedFalse(refreshToken).ifPresent(t -> {
            t.setRevoked(true);
            tokenRepo.save(t);
        });
    }

    private StudentAuthResponse buildAuthResponse(StudentAccount acc, String accessToken, String refreshToken) {
        return StudentAuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .studentAccountId(acc.getId().toString())
                .fullName(acc.getFullName())
                .accountStatus(acc.getStatus().name())
                .preferredLanguage(acc.getPreferredLanguage().name())
                .isLinked(acc.getStudent() != null)
                .school(StudentAuthResponse.SchoolInfo.builder()
                        .id(acc.getSchool().getId().toString())
                        .name(acc.getSchool().getName())
                        .country(acc.getSchool().getCountry())
                        .build())
                .build();
    }
}
