package com.edukira.service.impl;

import com.edukira.dto.request.StudentRequest;
import com.edukira.dto.response.StudentResponse;
import com.edukira.entity.School;
import com.edukira.entity.Student;
import com.edukira.entity.Subscription;
import com.edukira.enums.Language;
import com.edukira.enums.StudentStatus;
import com.edukira.exception.EdukiraException;
import com.edukira.repository.SchoolRepository;
import com.edukira.repository.StudentRepository;
import com.edukira.repository.SubscriptionRepository;
import com.edukira.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentServiceImpl implements StudentService {

    private final StudentRepository      studentRepo;
    private final SchoolRepository       schoolRepo;
    private final SubscriptionRepository subscriptionRepo;

    @Override
    @Transactional
    public StudentResponse create(StudentRequest req, UUID schoolId) {

        School school = schoolRepo.findById(schoolId)
                .orElseThrow(() -> EdukiraException.notFound("Escola"));

        // ── Verificação de plano / limite de alunos ──────────────────
        Subscription sub = subscriptionRepo.findBySchoolId(schoolId)
                .orElseThrow(() -> new EdukiraException(
                        "Assinatura não encontrada para esta escola.", HttpStatus.PAYMENT_REQUIRED));

        if (!sub.isActive()) {
            throw new EdukiraException(
                    "A assinatura da escola expirou. Renove o plano para continuar.",
                    HttpStatus.PAYMENT_REQUIRED);
        }

        long currentCount = studentRepo.countBySchoolIdAndStatus(schoolId, StudentStatus.ACTIVE);

        if (!sub.hasCapacity(currentCount)) {
            String msg = switch (sub.getPlan()) {
                case STARTER -> String.format(
                        "Limite de %d alunos atingido no plano Starter. " +
                                "Faça upgrade para Pro (até 1.000 alunos).", sub.getStudentLimit());
                case PRO -> String.format(
                        "Limite de %d alunos atingido no plano Pro. " +
                                "Faça upgrade para Enterprise (ilimitado).", sub.getStudentLimit());
                case ENTERPRISE -> "Limite atingido."; // nunca deve chegar aqui
            };
            throw new EdukiraException(msg, HttpStatus.FORBIDDEN);
        }

        // ── Criar aluno ──────────────────────────────────────────────
        Student student = Student.builder()
                .school(school)
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .classLevel(req.getClassLevel())
                .guardianName(req.getGuardianName())
                .guardianPhone(req.getGuardianPhone())
                .guardianEmail(req.getGuardianEmail())
                .guardianLanguage(req.getGuardianLanguage() != null
                        ? req.getGuardianLanguage() : Language.fr)
                .status(StudentStatus.ACTIVE)
                .enrollmentDate(req.getEnrollmentDate())
                .build();

        studentRepo.save(student);

        log.info("[STUDENT] Criado | id={} escola={} plano={} vagas_restantes={}",
                student.getId(), schoolId, sub.getPlan(),
                sub.remainingSlots(currentCount + 1));

        return toResponse(student);
    }

    @Override
    public Page<StudentResponse> findAll(UUID schoolId, Pageable pageable) {
        return studentRepo.findBySchoolId(schoolId, pageable).map(this::toResponse);
    }

    @Override
    public StudentResponse findById(UUID id, UUID schoolId) {
        return toResponse(studentRepo.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> EdukiraException.notFound("Aluno")));
    }

    @Override
    @Transactional
    public StudentResponse update(UUID id, StudentRequest req, UUID schoolId) {
        Student student = studentRepo.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> EdukiraException.notFound("Aluno"));

        student.setFirstName(req.getFirstName());
        student.setLastName(req.getLastName());
        student.setClassLevel(req.getClassLevel());
        student.setGuardianName(req.getGuardianName());
        student.setGuardianPhone(req.getGuardianPhone());
        student.setGuardianEmail(req.getGuardianEmail());
        if (req.getGuardianLanguage() != null) {
            student.setGuardianLanguage(req.getGuardianLanguage());
        }
        studentRepo.save(student);
        return toResponse(student);
    }

    @Override
    @Transactional
    public void delete(UUID id, UUID schoolId) {
        Student student = studentRepo.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> EdukiraException.notFound("Aluno"));
        // Soft delete — apenas inactiva
        student.setStatus(StudentStatus.INACTIVE);
        studentRepo.save(student);
        log.info("[STUDENT] Inactivado | id={}", id);
    }

    private StudentResponse toResponse(Student s) {
        return StudentResponse.builder()
                .id(s.getId())
                .firstName(s.getFirstName())
                .lastName(s.getLastName())
                .classLevel(s.getClassLevel())
                .guardianName(s.getGuardianName())
                .guardianPhone(s.getGuardianPhone())
                .guardianEmail(s.getGuardianEmail())
                .status(s.getStatus())
                .enrollmentDate(s.getEnrollmentDate())
                .createdAt(s.getCreatedAt())
                .build();
    }
}