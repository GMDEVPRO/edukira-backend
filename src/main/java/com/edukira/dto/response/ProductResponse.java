package com.edukira.dto.response;

import com.edukira.enums.ProductCategory;
import com.edukira.enums.ProductStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data @Builder
public class ProductResponse {
    private UUID id;
    private String title;
    private String description;
    private ProductCategory category;
    private String subject;
    private String level;
    private BigDecimal price;
    private String currency;
    private String previewUrl;
    private String thumbnailUrl;
    private ProductStatus status;
    private Integer downloads;
    private Double averageRating;
    private String sellerName;
    private Instant createdAt;
}
