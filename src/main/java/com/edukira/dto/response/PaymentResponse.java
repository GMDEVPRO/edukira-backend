package com.edukira.dto.response;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data @Builder
public class PaymentResponse {
    private String id;
    private String studentId;
    private String studentName;
    private BigDecimal amount;
    private String currency;
    private String month;
    private String status;
    private String method;
    private String receiptUrl;
    private Instant paidAt;
}
