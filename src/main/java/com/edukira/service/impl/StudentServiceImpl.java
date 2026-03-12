package com.edukira.service.impl;

import com.edukira.dto.request.StudentRequest;
import com.edukira.dto.response.StudentResponse;
import com.edukira.entity.School;
import com.edukira.entity.Student;
import com.edukira.enums.StudentStatus;
import com.edukira.exception.EdukiraException;
import com.edukira.repository.SchoolRepository;
import com.edukira.repository.StudentRepository;
import com.edukira.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepo;
    private final SchoolRepository schoolRepo;

    @Override
    public Page<StudentResponse> findAll(UUID schoolId, Pageable pageable) {
        return studentRepo.findBySchoolIdAndStatus(schoolId, StudentStatus.ACTIVE, pageable)
                .map(this::toResponse);
    }

    @Override
    public StudentResponse findById(UUID id, UUID schoolId) {
        return studentRepo.findByIdAndSchoolId(id, schoolId)
                .map(this::toResponse)
                .orElseThrow(() -> EdukiraException.notFound("Aluno"));
    }

    @Override
    @Transactional
    public StudentResponse create(StudentRequest req, UUID schoolId) {
        School school = schoolRepo.findById(schoolId)
                .orElseThrow(() -> EdukiraException.notFound("Escola"));

        Student student = Student.builder()
                .school(school)
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .classLevel(req.getClassLevel())
                .guardianName(req.getGuardianName())
                .guardianPhone(req.getGuardianPhone())
                .guardianEmail(req.getGuardianEmail())
                .guardianLanguage(req.getGuardianLanguage())
                .enrollmentDate(req.getEnrollmentDate())
                .build();

        return toResponse(studentRepo.save(student));
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
        if (req.getGuardianLanguage() != null) student.setGuardianLanguage(req.getGuardianLanguage());

        return toResponse(studentRepo.save(student));
    }

    @Override
    @Transactional
    public void delete(UUID id, UUID schoolId) {
        Student student = studentRepo.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> EdukiraException.notFound("Aluno"));
        student.setStatus(StudentStatus.INACTIVE);
        studentRepo.save(student);
    }

    private StudentResponse toResponse(Student s) {
        return StudentResponse.builder()
                .id(s.getId().toString())
                .firstName(s.getFirstName())
                .lastName(s.getLastName())
                .classLevel(s.getClassLevel())
                .guardianName(s.getGuardianName())
                .guardianPhone(s.getGuardianPhone())
                .guardianLanguage(s.getGuardianLanguage() != null ? s.getGuardianLanguage().name() : null)
                .status(s.getStatus().name())
                .enrollmentDate(s.getEnrollmentDate())
                .build();
    }
}
