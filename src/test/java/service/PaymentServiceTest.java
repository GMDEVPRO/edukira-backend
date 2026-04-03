package com.edukira.service;

import com.edukira.dto.request.PaymentInitRequest;
import com.edukira.dto.response.PaymentResponse;
import com.edukira.entity.Payment;
import com.edukira.entity.School;
import com.edukira.entity.Student;
import com.edukira.enums.PaymentMethod;
import com.edukira.enums.PaymentStatus;
import com.edukira.exception.EdukiraException;
import com.edukira.repository.PaymentRepository;
import com.edukira.repository.SchoolRepository;
import com.edukira.repository.StudentRepository;
import com.edukira.service.NotificationService;
import com.edukira.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService — testes unitários")
class PaymentServiceTest {

    @Mock PaymentRepository     paymentRepo;
    @Mock StudentRepository     studentRepo;
    @Mock SchoolRepository      schoolRepo;
    @Mock
    NotificationService notificationService;

    @InjectMocks PaymentServiceImpl paymentService;

    private School  school;
    private Student student;
    private UUID    schoolId;
    private UUID    studentId;

    @BeforeEach
    void setUp() {
        schoolId  = UUID.randomUUID();
        studentId = UUID.randomUUID();

        school = School.builder().id(schoolId).name("École Test").country("SN").build();
        student = Student.builder()
                .id(studentId).school(school)
                .firstName("Fatou").lastName("Mbaye")
                .guardianPhone("+221770000000").guardianName("Mme Mbaye")
                .build();

        // Injectar valores @Value manualmente
        ReflectionTestUtils.setField(paymentService, "globalWaveApiKey", "");
        ReflectionTestUtils.setField(paymentService, "globalOrangeClientId", "");
        ReflectionTestUtils.setField(paymentService, "globalOrangeClientSecret", "");
        ReflectionTestUtils.setField(paymentService, "globalMtnSubscriptionKey", "");
        ReflectionTestUtils.setField(paymentService, "mtnEnvironment", "sandbox");
    }

    // ── WAVE SANDBOX ─────────────────────────────────────────────────

    @Test
    @DisplayName("initMobileMoney WAVE sandbox — retorna checkout_url mock")
    void initMobileMoney_wave_sandbox_returnsMockUrl() {
        PaymentInitRequest req = buildRequest(PaymentMethod.WAVE, "500.00");

        when(schoolRepo.findById(schoolId)).thenReturn(Optional.of(school));
        when(studentRepo.findByIdAndSchoolId(studentId, schoolId)).thenReturn(Optional.of(student));
        when(paymentRepo.findBySchoolIdAndStatus(schoolId, PaymentStatus.PENDING))
                .thenReturn(Collections.emptyList());
        when(paymentRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        Map<String, Object> result = paymentService.initMobileMoney(req, schoolId);

        assertThat(result).containsKey("checkout_url");
        assertThat(result.get("provider")).isEqualTo("WAVE");
        assertThat(result.get("sandbox")).isEqualTo(true);
        verify(paymentRepo, times(1)).save(any());
    }

    // ── ORANGE MONEY SANDBOX ─────────────────────────────────────────

    @Test
    @DisplayName("initMobileMoney ORANGE sandbox — retorna payment_url mock")
    void initMobileMoney_orange_sandbox_returnsMockUrl() {
        PaymentInitRequest req = buildRequest(PaymentMethod.ORANGE_MONEY, "1000.00");

        when(schoolRepo.findById(schoolId)).thenReturn(Optional.of(school));
        when(studentRepo.findByIdAndSchoolId(studentId, schoolId)).thenReturn(Optional.of(student));
        when(paymentRepo.findBySchoolIdAndStatus(schoolId, PaymentStatus.PENDING))
                .thenReturn(Collections.emptyList());
        when(paymentRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        Map<String, Object> result = paymentService.initMobileMoney(req, schoolId);

        assertThat(result.get("provider")).isEqualTo("ORANGE_MONEY");
        assertThat(result.get("sandbox")).isEqualTo(true);
        assertThat(result).containsKey("payment_url");
    }

    // ── MTN MOMO SANDBOX ─────────────────────────────────────────────

    @Test
    @DisplayName("initMobileMoney MTN sandbox — retorna reference_id mock")
    void initMobileMoney_mtn_sandbox_returnsMockRef() {
        PaymentInitRequest req = buildRequest(PaymentMethod.MTN_MOMO, "2000.00");

        when(schoolRepo.findById(schoolId)).thenReturn(Optional.of(school));
        when(studentRepo.findByIdAndSchoolId(studentId, schoolId)).thenReturn(Optional.of(student));
        when(paymentRepo.findBySchoolIdAndStatus(schoolId, PaymentStatus.PENDING))
                .thenReturn(Collections.emptyList());
        when(paymentRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        Map<String, Object> result = paymentService.initMobileMoney(req, schoolId);

        assertThat(result.get("provider")).isEqualTo("MTN_MOMO");
        assertThat(result.get("sandbox")).isEqualTo(true);
        assertThat(result).containsKey("reference_id");
    }

    // ── CASH ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("initMobileMoney CASH — marca como PAID imediatamente")
    void initMobileMoney_cash_markedAsPaidImmediately() {
        PaymentInitRequest req = buildRequest(PaymentMethod.CASH, "300.00");

        when(schoolRepo.findById(schoolId)).thenReturn(Optional.of(school));
        when(studentRepo.findByIdAndSchoolId(studentId, schoolId)).thenReturn(Optional.of(student));
        when(paymentRepo.findBySchoolIdAndStatus(schoolId, PaymentStatus.PENDING))
                .thenReturn(Collections.emptyList());
        when(paymentRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        Map<String, Object> result = paymentService.initMobileMoney(req, schoolId);

        assertThat(result.get("provider")).isEqualTo("CASH");
        assertThat(result.get("status")).isEqualTo("PAID");
    }

    // ── PAGAMENTO DUPLICADO ───────────────────────────────────────────

    @Test
    @DisplayName("init com pagamento pendente no mesmo mês lança 400")
    void initMobileMoney_duplicatePending_throws400() {
        PaymentInitRequest req = buildRequest(PaymentMethod.WAVE, "500.00");
        req.setMonth("2026-03");

        Payment existing = Payment.builder()
                .id(UUID.randomUUID())
                .student(student).school(school)
                .month("2026-03")
                .status(PaymentStatus.PENDING)
                .sessionId("existingSession")
                .build();

        when(schoolRepo.findById(schoolId)).thenReturn(Optional.of(school));
        when(studentRepo.findByIdAndSchoolId(studentId, schoolId)).thenReturn(Optional.of(student));
        when(paymentRepo.findBySchoolIdAndStatus(schoolId, PaymentStatus.PENDING))
                .thenReturn(List.of(existing));

        assertThatThrownBy(() -> paymentService.initMobileMoney(req, schoolId))
                .isInstanceOf(EdukiraException.class)
                .hasMessageContaining("pendente");
    }

    // ── WEBHOOK WAVE ─────────────────────────────────────────────────

    @Test
    @DisplayName("processWebhook WAVE checkout.session.completed — marca como PAID")
    void processWebhook_wave_completed_marksAsPaid() {
        String sessionId = "cos_WAVE_abc123";

        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .student(student).school(school)
                .month("2026-03")
                .amount(new BigDecimal("500.00"))
                .currency("XOF")
                .status(PaymentStatus.PENDING)
                .sessionId(sessionId)
                .build();

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "checkout.session.completed");
        payload.put("data", Map.of("id", sessionId, "transaction_id", "txn_001"));

        when(paymentRepo.findBySessionId(sessionId)).thenReturn(Optional.of(payment));
        when(paymentRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        doNothing().when(notificationService).notifyPaymentConfirmed(
                any(), any(), any(), any(), any(), any(), any(), any());

        paymentService.processWebhook("WAVE", payload);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(payment.getPaidAt()).isNotNull();
        assertThat(payment.getTransactionId()).isEqualTo("txn_001");
        verify(paymentRepo, times(1)).save(payment);
    }

    @Test
    @DisplayName("processWebhook WAVE evento ignorado não altera pagamento")
    void processWebhook_wave_ignoredEvent_doesNothing() {
        Map<String, Object> payload = Map.of("type", "checkout.session.expired");

        paymentService.processWebhook("WAVE", payload);

        verify(paymentRepo, never()).save(any());
    }

    // ── OVERDUE ──────────────────────────────────────────────────────

    @Test
    @DisplayName("findOverdue retorna apenas pagamentos OVERDUE da escola")
    void findOverdue_returnsOnlyOverdueForSchool() {
        Payment overdue = Payment.builder()
                .id(UUID.randomUUID()).student(student).school(school)
                .amount(new BigDecimal("500")).currency("XOF")
                .month("2026-01").status(PaymentStatus.OVERDUE)
                .method(PaymentMethod.WAVE)
                .build();

        when(paymentRepo.findBySchoolIdAndStatus(schoolId, PaymentStatus.OVERDUE))
                .thenReturn(List.of(overdue));

        List<PaymentResponse> result = paymentService.findOverdue(schoolId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("OVERDUE");
    }

    // ── ESCOLA / ALUNO NÃO ENCONTRADO ────────────────────────────────

    @Test
    @DisplayName("init com escola inexistente lança 404")
    void initMobileMoney_unknownSchool_throws404() {
        PaymentInitRequest req = buildRequest(PaymentMethod.WAVE, "500.00");
        when(schoolRepo.findById(schoolId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.initMobileMoney(req, schoolId))
                .isInstanceOf(EdukiraException.class);
    }

    @Test
    @DisplayName("init com aluno inexistente lança 404")
    void initMobileMoney_unknownStudent_throws404() {
        PaymentInitRequest req = buildRequest(PaymentMethod.WAVE, "500.00");
        when(schoolRepo.findById(schoolId)).thenReturn(Optional.of(school));
        when(studentRepo.findByIdAndSchoolId(studentId, schoolId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.initMobileMoney(req, schoolId))
                .isInstanceOf(EdukiraException.class);
    }

    // ── Helper ───────────────────────────────────────────────────────

    private PaymentInitRequest buildRequest(PaymentMethod method, String amount) {
        PaymentInitRequest req = new PaymentInitRequest();
        req.setStudentId(studentId);
        req.setAmount(new BigDecimal(amount));
        req.setCurrency("XOF");
        req.setMethod(method);
        req.setPhone("+221770000000");
        return req;
    }
}
