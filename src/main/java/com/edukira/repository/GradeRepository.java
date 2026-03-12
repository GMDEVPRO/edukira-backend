package com.edukira.repository;
import com.edukira.entity.Grade;
import com.edukira.enums.GradePeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface GradeRepository extends JpaRepository<Grade, UUID> {
    List<Grade> findByStudentIdAndPeriodAndYear(UUID studentId, GradePeriod period, String year);
    List<Grade> findBySchoolIdAndStudentClassLevelAndPeriodAndYear(UUID schoolId, String classLevel, GradePeriod period, String year);
    List<Grade> findBySchoolIdAndUpdatedAtAfter(UUID schoolId, Instant since);
}
