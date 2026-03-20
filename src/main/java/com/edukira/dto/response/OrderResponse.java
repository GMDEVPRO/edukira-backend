package com.edukira.dto.response;

import com.edukira.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data @Builder
public class OrderResponse {
    private UUID id;
    private UUID productId;
    private String productTitle;
    private BigDecimal amountPaid;
    private String currency;
    private BigDecimal commissionAmt;
    private BigDecimal sellerAmount;
    private String paymentMethod;
    private OrderStatus status;
    private String downloadUrl;
    private Instant purchasedAt;
}
