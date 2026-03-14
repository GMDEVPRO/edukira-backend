package com.edukira.repository;

import com.edukira.entity.Enrollment;
import com.edukira.enums.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    Page<Enrollment> findBySchoolId(UUID schoolId, Pageable pageable);

    Page<Enrollment> findBySchoolIdAndStatus(UUID schoolId, EnrollmentStatus status, Pageable pageable);

    Optional<Enrollment> findByIdAndSchoolId(UUID id, UUID schoolId);

    long countBySchoolIdAndStatus(UUID schoolId, EnrollmentStatus status);
}
