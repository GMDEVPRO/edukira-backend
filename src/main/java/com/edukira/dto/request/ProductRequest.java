package com.edukira.dto.request;

import com.edukira.enums.Language;
import com.edukira.enums.ProductCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductRequest {
    @NotBlank
    private String title;
    private String description;
    @NotNull
    private ProductCategory category;
    private String subject;
    private String level;
    private Language language;
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;
    private String currency;
    private String fileUrl;
    private String previewUrl;
    private String thumbnailUrl;
    private Integer pages;
}
