package com.edukira.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardResponse {

    private SchoolStats schoolStats;
    private FinancialStats financialStats;
    private AcademicStats academicStats;
    private AttendanceStats attendanceStats;
    private List<RecentActivity> recentActivities;
    private List<AlertItem> alerts;

    @Data @Builder
    public static class SchoolStats {
        private long totalStudents;
        private long activeStudents;
        private long totalClasses;
        private long pendingApprovals;
    }

    @Data @Builder
    public static class FinancialStats {
        private double monthlyRevenue;
        private double overdueAmount;
        private long overdueCount;
        private double collectionRate;
    }

    @Data @Builder
    public static class AcademicStats {
        private double averageGrade;
        private double passRate;
        private long publishedReports;
        private String bestClass;
    }

    @Data @Builder
    public static class AttendanceStats {
        private double todayRate;
        private double monthRate;
        private long absentToday;
        private long atRiskStudents;
    }

    @Data @Builder
    public static class RecentActivity {
        private String type;
        private String description;
        private String time;
    }

    @Data @Builder
    public static class AlertItem {
        private String level;
        private String message;
    }
}
