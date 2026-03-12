package com.edukira.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data @Builder
public class StudentPaymentPortalResponse {
    private List<PaidPayment> paid;
    private List<UpcomingPayment> upcoming;
    private List<OverduePayment> overdue;
    private BigDecimal totalPaidYear;
    private BigDecimal totalDueYear;

    @Data @Builder
    public static class PaidPayment {
        private String id;
        private String month;
        private String amount;
        private String currency;
        private String method;
        private Instant paidAt;
        private String receiptUrl;
    }

    @Data @Builder
    public static class UpcomingPayment {
        private String month;
        private String amount;
        private String currency;
        private Instant dueDate;
        private String status;
    }

    @Data @Builder
    public static class OverduePayment {
        private String id;
        private String month;
        private String amount;
        private String currency;
        private long daysOverdue;
    }
}
