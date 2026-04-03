package com.edukira.dto.response;

import com.edukira.enums.PlanType;
import com.edukira.enums.SubscriptionStatus;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SubscriptionResponse {
    private UUID   id;
    private UUID   schoolId;
    private PlanType plan;
    private SubscriptionStatus status;
    private int    studentLimit;
    private long   currentStudentCount;
    private int    remainingSlots;    // -1 = ilimitado
    private boolean active;
    private Instant trialEndsAt;
    private Instant currentPeriodEnd;
    private Instant createdAt;
}
