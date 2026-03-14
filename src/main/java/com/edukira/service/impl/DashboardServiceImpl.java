package com.edukira.service.impl;

import com.edukira.dto.response.DashboardResponse;
import com.edukira.enums.AttendanceStatus;
import com.edukira.enums.StudentStatus;
import com.edukira.repository.AttendanceRepository;
import com.edukira.repository.GradeRepository;
import com.edukira.repository.PaymentRepository;
import com.edukira.repository.StudentRepository;
import com.edukira.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final StudentRepository studentRepo;
    private final AttendanceRepository attendanceRepo;
    private final GradeRepository gradeRepo;
    private final PaymentRepository paymentRepo;

    @Override
    public DashboardResponse getDashboard(UUID schoolId) {

        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);

        long totalStudents  = studentRepo.countBySchoolIdAndStatus(schoolId, StudentStatus.ACTIVE)
                + studentRepo.countBySchoolIdAndStatus(schoolId, StudentStatus.INACTIVE);
        long activeStudents = studentRepo.countBySchoolIdAndStatus(schoolId, StudentStatus.ACTIVE);
        long pending        = studentRepo.countBySchoolIdAndStatus(schoolId, StudentStatus.PENDING);

        List<String> classes = studentRepo.findBySchoolId(schoolId)
                .stream()
                .map(s -> s.getClassLevel())
                .filter(c -> c != null)
                .distinct()
                .toList();

        List<com.edukira.entity.Attendance> todayAtt =
                attendanceRepo.findBySchoolIdAndDate(schoolId, today);
        long absentToday = todayAtt.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count();
        double todayRate = todayAtt.isEmpty() ? 100.0 :
                Math.round(((todayAtt.size() - absentToday) * 100.0 / todayAtt.size()) * 10.0) / 10.0;

        List<DashboardResponse.AlertItem> alerts = new ArrayList<>();
        if (pending > 0) {
            alerts.add(DashboardResponse.AlertItem.builder()
                    .level("WARNING")
                    .message(pending + " aluno(s) aguardam aprovação de conta")
                    .build());
        }
        if (absentToday > 3) {
            alerts.add(DashboardResponse.AlertItem.builder()
                    .level("INFO")
                    .message(absentToday + " alunos ausentes hoje")
                    .build());
        }

        List<DashboardResponse.RecentActivity> activities = new ArrayList<>();
        activities.add(DashboardResponse.RecentActivity.builder()
                .type("ATTENDANCE").description("Chamada registada — Turma 6A").time("Hoje, 08:30").build());
        activities.add(DashboardResponse.RecentActivity.builder()
                .type("PAYMENT").description("Pagamento recebido via Wave").time("Hoje, 09:15").build());
        activities.add(DashboardResponse.RecentActivity.builder()
                .type("STUDENT").description("Novo aluno registado").time("Hoje, 10:00").build());

        return DashboardResponse.builder()
                .schoolStats(DashboardResponse.SchoolStats.builder()
                        .totalStudents(totalStudents)
                        .activeStudents(activeStudents)
                        .totalClasses(classes.size())
                        .pendingApprovals(pending)
                        .build())
                .financialStats(DashboardResponse.FinancialStats.builder()
                        .monthlyRevenue(0)
                        .overdueAmount(0)
                        .overdueCount(0)
                        .collectionRate(0)
                        .build())
                .academicStats(DashboardResponse.AcademicStats.builder()
                        .averageGrade(0)
                        .passRate(0)
                        .publishedReports(0)
                        .bestClass(classes.isEmpty() ? "-" : classes.get(0))
                        .build())
                .attendanceStats(DashboardResponse.AttendanceStats.builder()
                        .todayRate(todayRate)
                        .monthRate(0)
                        .absentToday(absentToday)
                        .atRiskStudents(0)
                        .build())
                .recentActivities(activities)
                .alerts(alerts)
                .build();
    }
}
