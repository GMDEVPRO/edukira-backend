package com.edukira.repository;
import com.edukira.entity.Student;
import com.edukira.enums.StudentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {
    Page<Student> findBySchoolIdAndStatus(UUID schoolId, StudentStatus status, Pageable pageable);
    List<Student> findBySchoolIdAndClassLevel(UUID schoolId, String classLevel);
    Optional<Student> findByIdAndSchoolId(UUID id, UUID schoolId);
    long countBySchoolIdAndStatus(UUID schoolId, StudentStatus status);

    @Query("SELECT s FROM Student s WHERE s.school.id = :schoolId AND s.updatedAt > :since")
    List<Student> findBySchoolIdAndUpdatedAtAfter(UUID schoolId, Instant since);
}
