package com.edukira.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data @Builder
public class StudentDashboardResponse {

    private StudentPortalResponse profile;

    // Resumo de notas
    private GradeSummary grades;

    // Resumo de pagamentos
    private PaymentSummary payments;

    // Mensagens não lidas
    private int unreadMessages;

    // Documentos disponíveis
    private int documentsAvailable;

    @Data @Builder
    public static class GradeSummary {
        private Double overallAverage;
        private String currentPeriod;
        private Integer rank;
        private Integer totalStudents;
        private String appreciation;       // Très bien, Bien, etc.
        private List<SubjectGrade> subjects;
    }

    @Data @Builder
    public static class SubjectGrade {
        private String subject;
        private Double grade1;
        private Double grade2;
        private Double average;
        private Double coefficient;
        private String period;
        private boolean published;
    }

    @Data @Builder
    public static class PaymentSummary {
        private BigDecimal totalPaid;
        private BigDecimal totalDue;
        private BigDecimal nextPaymentAmount;
        private String nextPaymentMonth;
        private String nextPaymentDueDate;
        private String paymentStatus;          // UP_TO_DATE | OVERDUE | PARTIAL
        private List<PaymentEntry> history;
        private List<PaymentEntry> upcoming;
    }

    @Data @Builder
    public static class PaymentEntry {
        private String id;
        private String month;
        private BigDecimal amount;
        private String currency;
        private String status;
        private String method;
        private String paidAt;
        private String receiptUrl;
    }
}
