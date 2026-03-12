package com.edukira.entity;

import com.edukira.enums.PaymentMethod;
import com.edukira.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 5)
    @Builder.Default
    private String currency = "XOF";

    @Column(nullable = false, length = 7)
    private String month;   // "2026-03"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Column(name = "session_id")
    private String sessionId;           // Wave / Orange pay_token / MTN referenceId

    @Column(name = "transaction_id")
    private String transactionId;       // ID final após confirmação

    @Column(name = "receipt_url")
    private String receiptUrl;

    @Column(name = "due_date")
    private Instant dueDate;

    @Column(name = "paid_at")
    private Instant paidAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
