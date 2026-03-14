package com.edukira.service.impl;

import com.edukira.dto.request.EnrollmentRequest;
import com.edukira.dto.request.EnrollmentReviewRequest;
import com.edukira.dto.response.EnrollmentResponse;
import com.edukira.entity.Enrollment;
import com.edukira.entity.School;
import com.edukira.entity.Student;
import com.edukira.entity.UserProfile;
import com.edukira.enums.EnrollmentStatus;
import com.edukira.enums.StudentStatus;
import com.edukira.exception.EdukiraException;
import com.edukira.repository.EnrollmentRepository;
import com.edukira.repository.SchoolRepository;
import com.edukira.repository.StudentRepository;
import com.edukira.repository.UserProfileRepository;
import com.edukira.service.EnrollmentService;
import com.edukira.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepo;
    private final SchoolRepository     schoolRepo;
    private final StudentRepository    studentRepo;
    private final UserProfileRepository userProfileRepo;

    @Override
    @Transactional
    public EnrollmentResponse submit(EnrollmentRequest req, UUID schoolId) {
        School school = schoolRepo.findById(schoolId)
                .orElseThrow(() -> EdukiraException.notFound("Escola"));

        Enrollment enrollment = Enrollment.builder()
                .school(school)
                .studentFirstName(req.getStudentFirstName())
                .studentLastName(req.getStudentLastName())
                .studentBirthDate(req.getStudentBirthDate())
                .studentGender(req.getStudentGender())
                .studentNationality(req.getStudentNationality())
                .classLevel(req.getClassLevel())
                .previousSchool(req.getPreviousSchool())
                .previousAverage(req.getPreviousAverage())
                .preferredLanguage(req.getPreferredLanguage())
                .guardianName(req.getGuardianName())
                .guardianPhone(req.getGuardianPhone())
                .guardianWhatsapp(req.getGuardianWhatsapp())
                .guardianEmail(req.getGuardianEmail())
                .guardianProfession(req.getGuardianProfession())
                .paymentMethod(req.getPaymentMethod())
                .paymentAmount(req.getPaymentAmount())
                .status(EnrollmentStatus.PENDING)
                .build();

        enrollmentRepo.save(enrollment);
        log.info("[ENROLLMENT] Nova matrícula submetida | escola={} aluno={} {}",
                schoolId, req.getStudentFirstName(), req.getStudentLastName());
        return toResponse(enrollment);
    }

    @Override
    public Page<EnrollmentResponse> findAll(UUID schoolId, EnrollmentStatus status, Pageable pageable) {
        if (status != null) {
            return enrollmentRepo.findBySchoolIdAndStatus(schoolId, status, pageable).map(this::toResponse);
        }
        return enrollmentRepo.findBySchoolId(schoolId, pageable).map(this::toResponse);
    }

    @Override
    public EnrollmentResponse findById(UUID id, UUID schoolId) {
        return toResponse(enrollmentRepo.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> EdukiraException.notFound("Matrícula")));
    }

    @Override
    @Transactional
    public EnrollmentResponse review(UUID id, EnrollmentReviewRequest req, UUID schoolId, UUID reviewerId) {
        Enrollment enrollment = enrollmentRepo.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> EdukiraException.notFound("Matrícula"));

        UserProfile reviewer = userProfileRepo.findById(reviewerId).orElse(null);
        enrollment.setReviewedBy(reviewer);
        enrollment.setReviewedAt(Instant.now());

        if (req.isApproved()) {
            enrollment.setStatus(EnrollmentStatus.APPROVED);

            Student student = Student.builder()
                    .school(enrollment.getSchool())
                    .firstName(enrollment.getStudentFirstName())
                    .lastName(enrollment.getStudentLastName())
                    .classLevel(enrollment.getClassLevel())
                    .guardianName(enrollment.getGuardianName())
                    .guardianPhone(enrollment.getGuardianPhone())
                    .guardianEmail(enrollment.getGuardianEmail())
                    .guardianLanguage(enrollment.getPreferredLanguage() != null
                            ? com.edukira.enums.Language.valueOf(enrollment.getPreferredLanguage())
                            : com.edukira.enums.Language.fr)
                    .enrollmentDate(java.time.LocalDate.now())
                    .status(StudentStatus.ACTIVE)
                    .build();

            studentRepo.save(student);
            enrollment.setStudent(student);
            log.info("[ENROLLMENT] Aprovada — aluno criado id={}", student.getId());
        } else {
            enrollment.setStatus(EnrollmentStatus.REJECTED);
            enrollment.setRejectionReason(req.getRejectionReason());
            log.info("[ENROLLMENT] Rejeitada id={} motivo={}", id, req.getRejectionReason());
        }

        enrollmentRepo.save(enrollment);
        return toResponse(enrollment);
    }

    @Override
    @Transactional
    public EnrollmentResponse confirmPayment(UUID id, UUID schoolId) {
        Enrollment enrollment = enrollmentRepo.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> EdukiraException.notFound("Matrícula"));

        enrollment.setPaymentConfirmed(true);
        enrollment.setStatus(EnrollmentStatus.PAYMENT_CONFIRMED);
        enrollmentRepo.save(enrollment);
        log.info("[ENROLLMENT] Pagamento confirmado id={}", id);
        return toResponse(enrollment);
    }

    private EnrollmentResponse toResponse(Enrollment e) {
        return EnrollmentResponse.builder()
                .id(e.getId())
                .studentFirstName(e.getStudentFirstName())
                .studentLastName(e.getStudentLastName())
                .classLevel(e.getClassLevel())
                .guardianName(e.getGuardianName())
                .guardianPhone(e.getGuardianPhone())
                .status(e.getStatus())
                .paymentMethod(e.getPaymentMethod())
                .paymentAmount(e.getPaymentAmount())
                .paymentConfirmed(e.isPaymentConfirmed())
                .rejectionReason(e.getRejectionReason())
                .createdAt(e.getCreatedAt())
                .reviewedAt(e.getReviewedAt())
                .build();
    }
}
