package com.edukira.dto.request;
import com.edukira.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PaymentInitRequest {
    @NotNull private UUID studentId;
    @NotNull private BigDecimal amount;
    private String currency = "XOF";
    private String month;           // "2026-03"
    @NotNull private PaymentMethod method;
    private String phone;           // para Mobile Money
}
