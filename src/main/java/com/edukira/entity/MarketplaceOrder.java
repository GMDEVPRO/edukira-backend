package com.edukira.entity;

import com.edukira.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "marketplace_orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MarketplaceOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private MarketplaceProduct product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_school_id")
    private School buyerSchool;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_user_id")
    private UserProfile buyerUser;

    @Column(name = "amount_paid", nullable = false, precision = 14, scale = 2)
    private BigDecimal amountPaid;

    @Column(nullable = false)
    private String currency;

    @Column(name = "commission_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionRate;

    @Column(name = "commission_amt", nullable = false, precision = 14, scale = 2)
    private BigDecimal commissionAmt;

    @Column(name = "seller_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal sellerAmount;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "payment_ref")
    private String paymentRef;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "download_url", columnDefinition = "TEXT")
    private String downloadUrl;

    @Column(name = "purchased_at", updatable = false)
    @Builder.Default
    private Instant purchasedAt = Instant.now();
}
