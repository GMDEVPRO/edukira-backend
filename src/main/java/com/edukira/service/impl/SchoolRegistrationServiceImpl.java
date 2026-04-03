package com.edukira.service.impl;

import com.edukira.dto.request.SchoolRegistrationRequest;
import com.edukira.dto.response.SchoolRegistrationResponse;
import com.edukira.entity.School;
import com.edukira.entity.Subscription;
import com.edukira.entity.UserProfile;
import com.edukira.enums.Language;
import com.edukira.enums.PlanType;
import com.edukira.enums.SubscriptionStatus;
import com.edukira.exception.EdukiraException;
import com.edukira.repository.SchoolRepository;
import com.edukira.repository.SubscriptionRepository;
import com.edukira.repository.UserProfileRepository;
import com.edukira.service.SchoolRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchoolRegistrationServiceImpl implements SchoolRegistrationService {

    private final SchoolRepository        schoolRepo;
    private final UserProfileRepository   userProfileRepo;
    private final SubscriptionRepository  subscriptionRepo;
    private final PasswordEncoder         passwordEncoder;

    // Limites por plano
    private static final int LIMIT_STARTER    = 200;
    private static final int LIMIT_PRO        = 1000;
    private static final int LIMIT_ENTERPRISE = -1;   // ilimitado

    @Override
    @Transactional
    public SchoolRegistrationResponse register(SchoolRegistrationRequest req) {

        if (userProfileRepo.existsByEmail(req.getAdminEmail())) {
            throw new EdukiraException(
                    "Email já registado: " + req.getAdminEmail(), HttpStatus.CONFLICT);
        }

        Language lang = req.getDefaultLanguage() != null
                ? req.getDefaultLanguage() : Language.fr;

        // ── 1. Criar escola ──────────────────────────────────────────
        School school = School.builder()
                .name(req.getSchoolName())
                .type(req.getSchoolType())
                .country(req.getCountry())
                .city(req.getCity())
                .defaultLanguage(lang)
                .active(true)
                .build();
        schoolRepo.save(school);

        // ── 2. Criar admin ───────────────────────────────────────────
        UserProfile admin = UserProfile.builder()
                .school(school)
                .firstName(req.getAdminFirstName())
                .lastName(req.getAdminLastName())
                .email(req.getAdminEmail())
                .passwordHash(passwordEncoder.encode(req.getAdminPassword()))
                .role(com.edukira.enums.Role.SCHOOL_ADMIN)
                .preferredLanguage(lang)
                .active(true)
                .build();
        userProfileRepo.save(admin);

        // ── 3. Criar assinatura com plano escolhido ──────────────────
        PlanType plan = parsePlan(req.getPlan());
        Instant  now  = Instant.now();

        Subscription subscription = Subscription.builder()
                .school(school)
                .plan(plan)
                .status(SubscriptionStatus.TRIAL)
                .studentLimit(limitFor(plan))
                .trialEndsAt(now.plus(30, ChronoUnit.DAYS))
                .currentPeriodStart(now)
                .build();
        subscriptionRepo.save(subscription);

        log.info("[REGISTRATION] Escola criada | id={} nome={} plano={} limite={}",
                school.getId(), school.getName(), plan, limitFor(plan));

        return SchoolRegistrationResponse.builder()
                .schoolId(school.getId())
                .schoolName(school.getName())
                .adminEmail(req.getAdminEmail())
                .plan(req.getPlan())
                .trialEndsAt(subscription.getTrialEndsAt())
                .status("PENDING_ACTIVATION")
                .message("Registo submetido! A sua conta será activada em menos de 24 horas.")
                .build();
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private PlanType parsePlan(String raw) {
        if (raw == null) return PlanType.STARTER;
        try {
            return PlanType.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PlanType.STARTER;
        }
    }

    private int limitFor(PlanType plan) {
        return switch (plan) {
            case STARTER    -> LIMIT_STARTER;
            case PRO        -> LIMIT_PRO;
            case ENTERPRISE -> LIMIT_ENTERPRISE;
        };
    }
}