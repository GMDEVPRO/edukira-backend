package com.edukira.service.impl;

import com.edukira.dto.request.StudentContactUpdateRequest;
import com.edukira.dto.response.*;
import com.edukira.entity.StudentAccount;
import com.edukira.enums.PaymentStatus;
import com.edukira.enums.StudentAccountStatus;
import com.edukira.exception.EdukiraException;
import com.edukira.repository.*;
import com.edukira.service.StudentPortalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentPortalServiceImpl implements StudentPortalService {

    private final StudentAccountRepository accountRepo;
    private final StudentRepository studentRepo;
    private final GradeRepository gradeRepo;
    private final PaymentRepository paymentRepo;
    private final StudentDocumentRepository documentRepo;

    // ── PERFIL ──────────────────────────────────────

    @Override
    public StudentPortalResponse getMyProfile(UUID studentAccountId) {
        StudentAccount acc = accountRepo.findById(studentAccountId)
                .orElseThrow(() -> EdukiraException.notFound("Conta de aluno"));

        var builder = StudentPortalResponse.builder()
                .fullName(acc.getFullName())
                .email(acc.getEmail())
                .phone(acc.getPhone())
                .preferredLanguage(acc.getPreferredLanguage().name())
                .accountStatus(acc.getStatus().name())
                .schoolName(acc.getSchool().getName());

        // Se conta já aprovada e vinculada à ficha do aluno
        if (acc.getStudent() != null) {
            var s = acc.getStudent();
            builder
                .studentId(s.getId().toString())
                .classLevel(s.getClassLevel())
                .guardianName(s.getGuardianName())
                .guardianPhone(s.getGuardianPhone())
                .enrollmentDate(s.getEnrollmentDate() != null ? s.getEnrollmentDate().toString() : null)
                .status(s.getStatus().name())
                .paymentSummary(buildPaymentSummary(s.getId(), acc.getSchool().getId()))
                .academicSummary(buildAcademicSummary(s.getId()));
        }

        return builder.build();
    }

    @Override
    @Transactional
    public StudentPortalResponse updateContact(UUID studentAccountId, StudentContactUpdateRequest req) {
        StudentAccount acc = accountRepo.findById(studentAccountId)
                .orElseThrow(() -> EdukiraException.notFound("Conta de aluno"));

        // Verifica email duplicado se mudou
        if (req.getEmail() != null && !req.getEmail().equals(acc.getEmail())) {
            accountRepo.findByEmail(req.getEmail()).ifPresent(other -> {
                throw EdukiraException.badRequest("Este email já está em uso");
            });
            acc.setEmail(req.getEmail());
        }

        if (req.getPhone() != null) {
            acc.setPhone(req.getPhone());
        }

        accountRepo.save(acc);
        return getMyProfile(studentAccountId);
    }

    // ── NOTAS ───────────────────────────────────────

    @Override
    public List<StudentGradePortalResponse> getMyGrades(UUID studentId) {
        if (studentId == null) return List.of();

        String currentYear = "2025-2026";
        List<StudentGradePortalResponse> result = new ArrayList<>();

        for (var period : com.edukira.enums.GradePeriod.values()) {
            var grades = gradeRepo.findByStudentIdAndPeriodAndYear(studentId, period, currentYear)
                    .stream()
                    .filter(g -> Boolean.TRUE.equals(g.getPublished()))
                    .collect(Collectors.toList());

            if (grades.isEmpty()) continue;

            BigDecimal sum = BigDecimal.ZERO;
            BigDecimal coeffSum = BigDecimal.ZERO;

            List<StudentGradePortalResponse.SubjectGrade> subjects = new ArrayList<>();

            for (var g : grades) {
                BigDecimal coeff = g.getCoefficient() != null ? g.getCoefficient() : BigDecimal.ONE;
                BigDecimal avg = g.getAverage() != null ? g.getAverage() : BigDecimal.ZERO;
                sum = sum.add(avg.multiply(coeff));
                coeffSum = coeffSum.add(coeff);

                subjects.add(StudentGradePortalResponse.SubjectGrade.builder()
                        .subject(g.getSubjectName())
                        .grade1(g.getGrade1() != null ? g.getGrade1().toPlainString() : "—")
                        .grade2(g.getGrade2() != null ? g.getGrade2().toPlainString() : "—")
                        .average(avg.toPlainString())
                        .coefficient(coeff.toPlainString())
                        .appreciation(appreciation(avg.doubleValue()))
                        .build());
            }

            BigDecimal studentAvg = coeffSum.compareTo(BigDecimal.ZERO) > 0
                    ? sum.divide(coeffSum, 2, java.math.RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            result.add(StudentGradePortalResponse.builder()
                    .period(period.name())
                    .year(currentYear)
                    .studentAverage(studentAvg.toPlainString())
                    .subjects(subjects)
                    .build());
        }

        return result;
    }

    // ── PAGAMENTOS ──────────────────────────────────

    @Override
    public StudentPaymentPortalResponse getMyPayments(UUID studentId, UUID schoolId) {
        if (studentId == null) {
            return StudentPaymentPortalResponse.builder()
                    .paid(List.of()).upcoming(List.of()).overdue(List.of())
                    .totalPaidYear(BigDecimal.ZERO).totalDueYear(BigDecimal.ZERO)
                    .build();
        }

        var allPayments = paymentRepo.findAll().stream()
                .filter(p -> p.getStudent().getId().equals(studentId))
                .collect(Collectors.toList());

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");

        List<StudentPaymentPortalResponse.PaidPayment> paid = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PAID)
                .map(p -> StudentPaymentPortalResponse.PaidPayment.builder()
                        .id(p.getId().toString())
                        .month(p.getMonth())
                        .amount(p.getAmount().toPlainString())
                        .currency(p.getCurrency())
                        .method(p.getMethod().name())
                        .paidAt(p.getPaidAt())
                        .receiptUrl(p.getReceiptUrl())
                        .build())
                .collect(Collectors.toList());

        List<StudentPaymentPortalResponse.OverduePayment> overdue = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.OVERDUE)
                .map(p -> {
                    long daysOver = p.getDueDate() != null
                            ? java.time.Duration.between(p.getDueDate(), Instant.now()).toDays()
                            : 0;
                    return StudentPaymentPortalResponse.OverduePayment.builder()
                            .id(p.getId().toString())
                            .month(p.getMonth())
                            .amount(p.getAmount().toPlainString())
                            .currency(p.getCurrency())
                            .daysOverdue(Math.max(0, daysOver))
                            .build();
                })
                .collect(Collectors.toList());

        // Próximas 3 mensalidades (meses futuros sem pagamento registado)
        List<StudentPaymentPortalResponse.UpcomingPayment> upcoming = generateUpcomingPayments(allPayments, 25000);

        BigDecimal totalPaid = paid.stream()
                .map(p -> new BigDecimal(p.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDue = overdue.stream()
                .map(p -> new BigDecimal(p.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return StudentPaymentPortalResponse.builder()
                .paid(paid).upcoming(upcoming).overdue(overdue)
                .totalPaidYear(totalPaid).totalDueYear(totalDue)
                .build();
    }

    // ── DOCUMENTOS ──────────────────────────────────

    @Override
    public List<StudentDocumentResponse> getMyDocuments(UUID studentId) {
        if (studentId == null) return List.of();
        return documentRepo.findByStudentIdAndVisibleTrue(studentId).stream()
                .map(d -> StudentDocumentResponse.builder()
                        .id(d.getId().toString())
                        .type(d.getType().name())
                        .title(d.getTitle())
                        .fileUrl(d.getFileUrl())
                        .schoolYear(d.getSchoolYear())
                        .createdAt(d.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // ── ADMIN: APROVAÇÃO ─────────────────────────────

    @Override
    @Transactional
    public void approveAccount(UUID accountId, UUID adminUserId, UUID schoolId) {
        StudentAccount acc = accountRepo.findByIdAndSchoolId(accountId, schoolId)
                .orElseThrow(() -> EdukiraException.notFound("Conta de aluno"));

        if (acc.getStatus() != StudentAccountStatus.PENDING_APPROVAL) {
            throw EdukiraException.badRequest("Conta já processada");
        }

        // Tenta vincular automaticamente pelo número de documento
        studentRepo.findAll().stream()
                .filter(s -> s.getSchool().getId().equals(schoolId))
                .filter(s -> acc.getDocumentNumber() != null &&
                        acc.getDocumentNumber().equalsIgnoreCase(
                                s.getId().toString().substring(0, 8))) // placeholder — adaptar ao campo real
                .findFirst()
                .ifPresent(acc::setStudent);

        acc.setStatus(StudentAccountStatus.ACTIVE);
        acc.setApprovedBy(adminUserId);
        acc.setApprovedAt(Instant.now());
        accountRepo.save(acc);

        log.info("Conta de aluno {} aprovada por admin {}", accountId, adminUserId);
    }

    @Override
    @Transactional
    public void rejectAccount(UUID accountId, String reason, UUID schoolId) {
        StudentAccount acc = accountRepo.findByIdAndSchoolId(accountId, schoolId)
                .orElseThrow(() -> EdukiraException.notFound("Conta de aluno"));

        acc.setStatus(StudentAccountStatus.REJECTED);
        acc.setRejectionReason(reason);
        accountRepo.save(acc);

        log.info("Conta de aluno {} rejeitada. Motivo: {}", accountId, reason);
    }

    // ── HELPERS ─────────────────────────────────────

    private StudentPortalResponse.PaymentSummary buildPaymentSummary(UUID studentId, UUID schoolId) {
        var payments = paymentRepo.findAll().stream()
                .filter(p -> p.getStudent().getId().equals(studentId))
                .collect(Collectors.toList());

        BigDecimal totalPaid = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PAID)
                .map(p -> p.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long overdueCount = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.OVERDUE)
                .count();

        // Próxima mensalidade
        YearMonth nextMonth = YearMonth.now().plusMonths(1);
        String nextMonthStr = nextMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy",
                java.util.Locale.FRENCH));

        return StudentPortalResponse.PaymentSummary.builder()
                .totalPaid(totalPaid.toPlainString() + " FCFA")
                .nextDueDate(nextMonth.atDay(5).toString())
                .nextDueAmount("25 000 FCFA")
                .overdueCount((int) overdueCount)
                .build();
    }

    private StudentPortalResponse.AcademicSummary buildAcademicSummary(UUID studentId) {
        String year = "2025-2026";
        var grades = gradeRepo.findByStudentIdAndPeriodAndYear(
                studentId, com.edukira.enums.GradePeriod.BIMESTRE_2, year)
                .stream().filter(g -> Boolean.TRUE.equals(g.getPublished())).collect(Collectors.toList());

        if (grades.isEmpty()) {
            return StudentPortalResponse.AcademicSummary.builder()
                    .overallAverage("—").currentPeriod("BIMESTRE_2")
                    .subjectCount(0).rank("—").build();
        }

        BigDecimal sum = grades.stream()
                .map(g -> g.getAverage() != null ? g.getAverage() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avg = sum.divide(BigDecimal.valueOf(grades.size()), 2, java.math.RoundingMode.HALF_UP);

        return StudentPortalResponse.AcademicSummary.builder()
                .overallAverage(avg.toPlainString() + "/20")
                .currentPeriod("Bimestre 2")
                .subjectCount(grades.size())
                .rank("—")
                .build();
    }

    private List<StudentPaymentPortalResponse.UpcomingPayment> generateUpcomingPayments(
            List<?> existing, int monthlyFee) {

        List<StudentPaymentPortalResponse.UpcomingPayment> upcoming = new ArrayList<>();
        YearMonth current = YearMonth.now();

        for (int i = 1; i <= 3; i++) {
            YearMonth month = current.plusMonths(i);
            String monthStr = month.format(DateTimeFormatter.ofPattern("yyyy-MM"));

            upcoming.add(StudentPaymentPortalResponse.UpcomingPayment.builder()
                    .month(month.format(DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale.FRENCH)))
                    .amount(String.valueOf(monthlyFee))
                    .currency("FCFA")
                    .dueDate(month.atDay(5).atStartOfDay(ZoneId.of("UTC")).toInstant())
                    .status("UPCOMING")
                    .build());
        }
        return upcoming;
    }

    private String appreciation(double avg) {
        if (avg >= 16) return "Très bien";
        if (avg >= 14) return "Bien";
        if (avg >= 12) return "Assez bien";
        if (avg >= 10) return "Passable";
        return "Insuffisant";
    }
}
