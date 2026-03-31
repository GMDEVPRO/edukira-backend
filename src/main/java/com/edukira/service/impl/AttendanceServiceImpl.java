package com.edukira.service.impl;

import com.edukira.dto.request.AttendanceRequest;
import com.edukira.dto.response.AttendanceResponse;
import com.edukira.dto.response.AttendanceSummaryResponse;
import com.edukira.entity.Attendance;
import com.edukira.entity.Student;
import com.edukira.entity.UserProfile;
import com.edukira.enums.AttendanceStatus;
import com.edukira.repository.AttendanceRepository;
import com.edukira.repository.StudentRepository;
import com.edukira.repository.UserProfileRepository;
import com.edukira.service.AttendanceService;
import com.edukira.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository   attendanceRepo;
    private final StudentRepository      studentRepo;
    private final UserProfileRepository  userProfileRepo;
    private final NotificationService    notificationService;

    @Override
    @Transactional
    public AttendanceSummaryResponse record(AttendanceRequest request, String classLevel, UUID schoolId, UUID userId) {
        UserProfile recorder = userProfileRepo.findById(userId).orElse(null);

        List<Attendance> saved = request.getEntries().stream().map(entry -> {
            Student student = studentRepo.findByIdAndSchoolId(entry.getStudentId(), schoolId)
                    .orElseThrow(() -> new RuntimeException("Aluno não encontrado: " + entry.getStudentId()));

            Attendance att = attendanceRepo
                    .findByStudentIdAndDateAndSubject(entry.getStudentId(), request.getDate(), request.getSubject())
                    .orElse(Attendance.builder()
                            .school(student.getSchool())
                            .student(student)
                            .date(request.getDate())
                            .subject(request.getSubject())
                            .build());

            att.setStatus(entry.getStatus());
            att.setObservation(entry.getObservation());
            att.setRecordedBy(recorder);
            Attendance saved2 = attendanceRepo.save(att);

            // ── Notificação ao responsável ───────────────────────────
            if (entry.getStatus() == AttendanceStatus.ABSENT) {
                notificationService.notifyAbsence(
                        schoolId, student.getId(),
                        student.getFirstName() + " " + student.getLastName(),
                        student.getGuardianPhone(), student.getGuardianName(),
                        request.getDate().toString(), request.getSubject());
            } else if (entry.getStatus() == AttendanceStatus.LATE) {
                notificationService.notifyLate(
                        schoolId, student.getId(),
                        student.getFirstName() + " " + student.getLastName(),
                        student.getGuardianPhone(), student.getGuardianName(),
                        request.getDate().toString());
            }
            return saved2;
        }).collect(Collectors.toList());

        return buildSummary(classLevel, request.getDate(), request.getSubject(), saved);
    }

    @Override
    public AttendanceSummaryResponse getByClassAndDate(String classLevel, LocalDate date, UUID schoolId) {
        List<Attendance> list = attendanceRepo.findBySchoolIdAndClassLevelAndDate(schoolId, classLevel, date);
        return buildSummary(classLevel, date, null, list);
    }

    @Override
    public List<AttendanceResponse> getByStudent(UUID studentId, LocalDate from, LocalDate to, UUID schoolId) {
        return attendanceRepo.findByStudentIdAndDateBetween(studentId, from, to)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public double getAttendanceRate(UUID studentId, LocalDate from, LocalDate to) {
        long total = attendanceRepo.countByStudentIdAndDateBetween(studentId, from, to);
        if (total == 0) return 100.0;
        long present = attendanceRepo.countByStudentIdAndStatusAndDateBetween(
                studentId, AttendanceStatus.PRESENT, from, to);
        long late = attendanceRepo.countByStudentIdAndStatusAndDateBetween(
                studentId, AttendanceStatus.LATE, from, to);
        return Math.round(((present + late) * 100.0 / total) * 10.0) / 10.0;
    }

    private AttendanceSummaryResponse buildSummary(String classLevel, LocalDate date,
                                                   String subject, List<Attendance> list) {
        int present = (int) list.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
        int absent  = (int) list.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count();
        int late    = (int) list.stream().filter(a -> a.getStatus() == AttendanceStatus.LATE).count();
        int excused = (int) list.stream().filter(a -> a.getStatus() == AttendanceStatus.EXCUSED).count();
        int total   = list.size();
        double rate = total == 0 ? 100.0 :
                Math.round(((present + late) * 100.0 / total) * 10.0) / 10.0;

        return AttendanceSummaryResponse.builder()
                .date(date)
                .classLevel(classLevel)
                .subject(subject)
                .totalStudents(total)
                .totalPresent(present)
                .totalAbsent(absent)
                .totalLate(late)
                .totalExcused(excused)
                .attendanceRate(rate)
                .entries(list.stream().map(this::toResponse).collect(Collectors.toList()))
                .build();
    }

    private AttendanceResponse toResponse(Attendance a) {
        return AttendanceResponse.builder()
                .id(a.getId())
                .studentId(a.getStudent().getId())
                .studentName(a.getStudent().getFirstName() + " " + a.getStudent().getLastName())
                .classLevel(a.getStudent().getClassLevel())
                .date(a.getDate())
                .subject(a.getSubject())
                .status(a.getStatus())
                .observation(a.getObservation())
                .build();
    }
}
