package com.edukira.service.impl;

import com.edukira.dto.request.SchoolRegistrationRequest;
import com.edukira.dto.response.SchoolRegistrationResponse;
import com.edukira.entity.School;
import com.edukira.entity.UserProfile;
import com.edukira.enums.Language;
import com.edukira.repository.SchoolRepository;
import com.edukira.repository.UserProfileRepository;
import com.edukira.service.SchoolRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchoolRegistrationServiceImpl implements SchoolRegistrationService {

    private final SchoolRepository      schoolRepo;
    private final UserProfileRepository userProfileRepo;
    private final PasswordEncoder       passwordEncoder;

    @Override
    @Transactional
    public SchoolRegistrationResponse register(SchoolRegistrationRequest req) {

        if (userProfileRepo.existsByEmail(req.getAdminEmail())) {
            throw new RuntimeException("Email já registado: " + req.getAdminEmail());
        }

        Language lang = req.getDefaultLanguage() != null
                ? req.getDefaultLanguage() : Language.fr;

        School school = School.builder()
                .name(req.getSchoolName())
                .type(req.getSchoolType())
                .country(req.getCountry())
                .city(req.getCity())
                .defaultLanguage(lang)
                .active(true)
                .build();

        schoolRepo.save(school);

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

        Instant trialEndsAt = Instant.now().plus(30, ChronoUnit.DAYS);

        log.info("[REGISTRATION] Nova escola registada | id={} nome={} plano={}",
                school.getId(), school.getName(), req.getPlan());

        return SchoolRegistrationResponse.builder()
                .schoolId(school.getId())
                .schoolName(school.getName())
                .adminEmail(req.getAdminEmail())
                .plan(req.getPlan())
                .trialEndsAt(trialEndsAt)
                .status("PENDING_ACTIVATION")
                .message("Registo submetido com sucesso! A sua conta será activada em menos de 24 horas.")
                .build();
    }
}