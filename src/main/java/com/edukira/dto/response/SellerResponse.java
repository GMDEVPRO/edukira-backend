package com.edukira.dto.response;

import com.edukira.enums.SellerStatus;
import com.edukira.enums.SellerType;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data @Builder
public class SellerResponse {
    private UUID id;
    private String displayName;
    private String bio;
    private SellerType sellerType;
    private BigDecimal commissionRate;
    private BigDecimal walletBalance;
    private SellerStatus status;
    private Instant createdAt;
}
