package com.edukira.repository;

import com.edukira.entity.Attendance;
import com.edukira.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {

    List<Attendance> findBySchoolIdAndDate(UUID schoolId, LocalDate date);

    List<Attendance> findByStudentIdAndDateBetween(UUID studentId, LocalDate from, LocalDate to);

    Optional<Attendance> findByStudentIdAndDateAndSubject(UUID studentId, LocalDate date, String subject);

    @Query("SELECT a FROM Attendance a WHERE a.school.id = :schoolId " +
            "AND a.student.classLevel = :classLevel AND a.date = :date")
    List<Attendance> findBySchoolIdAndClassLevelAndDate(UUID schoolId, String classLevel, LocalDate date);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.id = :studentId " +
            "AND a.status = :status AND a.date BETWEEN :from AND :to")
    long countByStudentIdAndStatusAndDateBetween(UUID studentId, AttendanceStatus status, LocalDate from, LocalDate to);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.id = :studentId " +
            "AND a.date BETWEEN :from AND :to")
    long countByStudentIdAndDateBetween(UUID studentId, LocalDate from, LocalDate to);
}
