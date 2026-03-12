package com.edukira.repository;

import com.edukira.entity.StudentAccount;
import com.edukira.enums.StudentAccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StudentAccountRepository extends JpaRepository<StudentAccount, UUID> {

    Optional<StudentAccount> findByEmailAndStatus(String email, StudentAccountStatus status);

    Optional<StudentAccount> findByEmail(String email);

    Optional<StudentAccount> findByDocumentNumberAndSchoolId(String documentNumber, UUID schoolId);

    boolean existsByEmailOrDocumentNumberAndSchoolId(String email, String documentNumber, UUID schoolId);

    Page<StudentAccount> findBySchoolIdAndStatus(UUID schoolId, StudentAccountStatus status, Pageable pageable);

    Optional<StudentAccount> findByIdAndSchoolId(UUID id, UUID schoolId);

    Optional<StudentAccount> findByStudentId(UUID studentId);
}
