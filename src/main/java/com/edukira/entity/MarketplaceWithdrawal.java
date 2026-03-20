package com.edukira.entity;

import com.edukira.enums.WithdrawalMethod;
import com.edukira.enums.WithdrawalStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "marketplace_withdrawals")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MarketplaceWithdrawal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private MarketplaceSeller seller;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "withdrawal_method", nullable = false)
    private WithdrawalMethod withdrawalMethod;

    // ── Mobile Money ──
    @Column(name = "mobile_operator")
    private String mobileOperator;

    @Column(name = "mobile_number")
    private String mobileNumber;

    // ── Bank Transfer ──
    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_account_name")
    private String bankAccountName;

    @Column(name = "bank_account_number")
    private String bankAccountNumber;

    @Column(name = "bank_iban")
    private String bankIban;

    @Column(name = "bank_swift")
    private String bankSwift;

    @Column(name = "bank_country")
    private String bankCountry;

    // ── Status ──
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private WithdrawalStatus status = WithdrawalStatus.PENDING;

    @Column
    private String reference;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "requested_at", updatable = false)
    private Instant requestedAt;

    @Column(name = "processed_at")
    private Instant processedAt;
}
