package com.edukira.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "commissions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Commission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Venda que gerou a comissão
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private MarketplaceOrder order;

    // Vendedor que pagou a comissão
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private MarketplaceSeller seller;

    @Column(name = "sale_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal saleAmount;          // valor bruto da venda

    @Column(name = "commission_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionRate;      // taxa %

    @Column(name = "commission_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal commissionAmount;    // valor retido pela Edukira

    @Column(name = "seller_net", nullable = false, precision = 14, scale = 2)
    private BigDecimal sellerNet;           // valor líquido creditado ao seller

    @Column(nullable = false, length = 5)
    @Builder.Default
    private String currency = "XOF";

    @Column(name = "product_title")
    private String productTitle;

    @Column(name = "seller_display_name")
    private String sellerDisplayName;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
