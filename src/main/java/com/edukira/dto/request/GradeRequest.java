package com.edukira.dto.request;
import com.edukira.enums.GradePeriod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class GradeRequest {
    @NotNull private UUID studentId;
    @NotBlank private String subjectName;
    private BigDecimal grade1;
    private BigDecimal grade2;
    private BigDecimal coefficient;
    @NotNull private GradePeriod period;
    @NotBlank private String year;
}
