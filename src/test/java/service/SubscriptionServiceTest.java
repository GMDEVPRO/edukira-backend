package com.edukira.service;

import com.edukira.entity.School;
import com.edukira.entity.Subscription;
import com.edukira.enums.PlanType;
import com.edukira.enums.StudentStatus;
import com.edukira.enums.SubscriptionStatus;
import com.edukira.exception.EdukiraException;
import com.edukira.repository.SchoolRepository;
import com.edukira.repository.StudentRepository;
import com.edukira.repository.SubscriptionRepository;
import com.edukira.service.impl.StudentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Subscription — verificação de limites por plano")
class SubscriptionServiceTest {

    @Mock StudentRepository      studentRepo;
    @Mock SchoolRepository       schoolRepo;
    @Mock SubscriptionRepository subscriptionRepo;

    @InjectMocks StudentServiceImpl studentService;

    private UUID   schoolId;
    private School school;

    @BeforeEach
    void setUp() {
        schoolId = UUID.randomUUID();
        school   = School.builder().id(schoolId).name("Escola Test").country("SN").build();
    }

    @Test
    @DisplayName("Starter com 200 alunos activos — bloqueia cadastro com 403")
    void starter_atLimit_blocksNewStudent() {
        Subscription sub = buildSub(PlanType.STARTER, 200, SubscriptionStatus.TRIAL,
                Instant.now().plusSeconds(86400));

        when(schoolRepo.findById(schoolId)).thenReturn(Optional.of(school));
        when(subscriptionRepo.findBySchoolId(schoolId)).thenReturn(Optional.of(sub));
        when(studentRepo.countBySchoolIdAndStatus(schoolId, StudentStatus.ACTIVE)).thenReturn(200L);

        assertThatThrownBy(() -> studentService.create(buildReq(), schoolId))
                .isInstanceOf(EdukiraException.class)
                .hasMessageContaining("Starter");
    }

    @Test
    @DisplayName("Pro com 999 alunos activos — permite cadastro")
    void pro_belowLimit_allowsNewStudent() {
        Subscription sub = buildSub(PlanType.PRO, 1000, SubscriptionStatus.ACTIVE,
                Instant.now().plusSeconds(86400 * 30));

        when(schoolRepo.findById(schoolId)).thenReturn(Optional.of(school));
        when(subscriptionRepo.findBySchoolId(schoolId)).thenReturn(Optional.of(sub));
        when(studentRepo.countBySchoolIdAndStatus(schoolId, StudentStatus.ACTIVE)).thenReturn(999L);
        when(studentRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        assertThatNoException().isThrownBy(() -> studentService.create(buildReq(), schoolId));
    }

    @Test
    @DisplayName("Pro com 1000 alunos activos — bloqueia cadastro com 403")
    void pro_atLimit_blocksNewStudent() {
        Subscription sub = buildSub(PlanType.PRO, 1000, SubscriptionStatus.ACTIVE,
                Instant.now().plusSeconds(86400 * 30));

        when(schoolRepo.findById(schoolId)).thenReturn(Optional.of(school));
        when(subscriptionRepo.findBySchoolId(schoolId)).thenReturn(Optional.of(sub));
        when(studentRepo.countBySchoolIdAndStatus(schoolId, StudentStatus.ACTIVE)).thenReturn(1000L);

        assertThatThrownBy(() -> studentService.create(buildReq(), schoolId))
                .isInstanceOf(EdukiraException.class)
                .hasMessageContaining("Pro");
    }

    @Test
    @DisplayName("Enterprise — nunca bloqueia independente do número de alunos")
    void enterprise_neverBlocks() {
        Subscription sub = buildSub(PlanType.ENTERPRISE, -1, SubscriptionStatus.ACTIVE,
                Instant.now().plusSeconds(86400 * 365));

        when(schoolRepo.findById(schoolId)).thenReturn(Optional.of(school));
        when(subscriptionRepo.findBySchoolId(schoolId)).thenReturn(Optional.of(sub));
        when(studentRepo.countBySchoolIdAndStatus(schoolId, StudentStatus.ACTIVE)).thenReturn(50000L);
        when(studentRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        assertThatNoException().isThrownBy(() -> studentService.create(buildReq(), schoolId));
    }

    @Test
    @DisplayName("Trial expirado — bloqueia cadastro com 402")
    void expiredTrial_blocksNewStudent() {
        Subscription sub = buildSub(PlanType.STARTER, 200, SubscriptionStatus.TRIAL,
                Instant.now().minusSeconds(3600)); // expirado há 1h

        when(schoolRepo.findById(schoolId)).thenReturn(Optional.of(school));
        when(subscriptionRepo.findBySchoolId(schoolId)).thenReturn(Optional.of(sub));

        assertThatThrownBy(() -> studentService.create(buildReq(), schoolId))
                .isInstanceOf(EdukiraException.class)
                .hasMessageContaining("expirou");
    }

    @Test
    @DisplayName("Sem assinatura — lança 402")
    void noSubscription_throws402() {
        when(schoolRepo.findById(schoolId)).thenReturn(Optional.of(school));
        when(subscriptionRepo.findBySchoolId(schoolId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.create(buildReq(), schoolId))
                .isInstanceOf(EdukiraException.class);
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private Subscription buildSub(PlanType plan, int limit,
                                  SubscriptionStatus status, Instant expiry) {
        Subscription sub = Subscription.builder()
                .id(UUID.randomUUID()).school(school)
                .plan(plan).studentLimit(limit).status(status)
                .build();
        if (status == SubscriptionStatus.TRIAL)  sub.setTrialEndsAt(expiry);
        if (status == SubscriptionStatus.ACTIVE) sub.setCurrentPeriodEnd(expiry);
        return sub;
    }

    private com.edukira.dto.request.StudentRequest buildReq() {
        com.edukira.dto.request.StudentRequest req = new com.edukira.dto.request.StudentRequest();
        req.setFirstName("Test");
        req.setLastName("Aluno");
        req.setClassLevel("6ème");
        return req;
    }
}