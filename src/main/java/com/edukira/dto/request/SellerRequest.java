package com.edukira.dto.request;

import com.edukira.enums.SellerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SellerRequest {
    @NotBlank
    private String displayName;
    private String bio;
    @NotNull
    private SellerType sellerType;
    private String mobileMoneyNumber;
    private String mobileMoneyOperator;
}
