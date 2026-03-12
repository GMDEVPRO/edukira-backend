package com.edukira.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data @Builder
public class StudentPortalResponse {

    // Dados do cadastro
    private String studentId;
    private String fullName;
    private String classLevel;
    private String schoolYear;
    private String schoolName;
    private String enrollmentDate;
    private String status;

    // Responsável
    private String guardianName;
    private String guardianPhone;

    // Conta
    private String email;
    private String phone;
    private String preferredLanguage;
    private String accountStatus;

    // Resumo financeiro
    private PaymentSummary paymentSummary;

    // Resumo académico
    private AcademicSummary academicSummary;

    @Data @Builder
    public static class PaymentSummary {
        private String totalPaid;
        private String totalDue;
        private String nextDueDate;
        private String nextDueAmount;
        private int overdueCount;
    }

    @Data @Builder
    public static class AcademicSummary {
        private String overallAverage;
        private String currentPeriod;
        private int subjectCount;
        private String rank;
    }
}
