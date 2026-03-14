package com.edukira.repository;

import com.edukira.entity.Grade;
import com.edukira.enums.GradePeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GradeRepository extends JpaRepository<Grade, UUID> {

    List<Grade> findByStudentIdAndPeriodAndYear(UUID studentId, GradePeriod period, String year);

    List<Grade> findByStudentIdAndYear(UUID studentId, String year);

    @Query("SELECT g FROM Grade g WHERE g.school.id = :schoolId " +
            "AND g.student.classLevel = :classLevel " +
            "AND g.period = :period AND g.year = :year")
    List<Grade> findBySchoolIdAndClassLevelAndPeriodAndYear(
            UUID schoolId, String classLevel, GradePeriod period, String year);

    Optional<Grade> findByStudentIdAndSubjectNameAndPeriodAndYear(
            UUID studentId, String subjectName, GradePeriod period, String year);

    Optional<Grade> findByIdAndSchoolId(UUID id, UUID schoolId);

    List<Grade> findBySchoolIdAndUpdatedAtAfter(UUID schoolId, Instant since);
}

