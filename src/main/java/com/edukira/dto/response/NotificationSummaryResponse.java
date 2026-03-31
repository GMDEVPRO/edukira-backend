package com.edukira.dto.response;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationSummaryResponse {
    private long totalSent;
    private long totalFailed;
    private long totalPending;
    private long absenceAlerts;
    private long gradeAlerts;
    private long paymentAlerts;
}
