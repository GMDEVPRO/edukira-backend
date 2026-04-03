package com.edukira.entity;

import com.edukira.enums.PlanType;
import com.edukira.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "subscriptions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false, unique = true)
    private School school;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanType plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SubscriptionStatus status = SubscriptionStatus.TRIAL;

    @Column(name = "student_limit", nullable = false)
    private int studentLimit;   // 200 | 1000 | -1 (ilimitado)

    @Column(name = "trial_ends_at")
    private Instant trialEndsAt;

    @Column(name = "current_period_start")
    private Instant currentPeriodStart;

    @Column(name = "current_period_end")
    private Instant currentPeriodEnd;

    // Referência ao pagamento externo (Stripe, Mobile Money, etc.)
    @Column(name = "external_ref")
    private String externalRef;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // ── Helpers ──────────────────────────────────────────────

    /** Verifica se a assinatura ainda está válida (trial ou activa) */
    public boolean isActive() {
        return switch (status) {
            case TRIAL   -> trialEndsAt != null && Instant.now().isBefore(trialEndsAt);
            case ACTIVE  -> currentPeriodEnd != null && Instant.now().isBefore(currentPeriodEnd);
            default      -> false;
        };
    }

    /** Verifica se ainda tem vagas para mais alunos */
    public boolean hasCapacity(long currentStudentCount) {
        if (studentLimit == -1) return true;  // ENTERPRISE — ilimitado
        return currentStudentCount < studentLimit;
    }

    /** Quantas vagas restam (-1 = ilimitado) */
    public int remainingSlots(long currentStudentCount) {
        if (studentLimit == -1) return -1;
        return Math.max(0, studentLimit - (int) currentStudentCount);
    }
}
