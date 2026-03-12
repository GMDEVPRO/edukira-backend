package com.edukira.dto.request;

import com.edukira.enums.GradePeriod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class GradeBatchRequest {
    @NotBlank
    private String classLevel;

    @NotBlank
    private String subjectName;

    @NotNull
    private GradePeriod period;

    @NotBlank
    private String year;

    private Double coefficient;

    @NotEmpty
    @Valid
    private List<GradeEntry> grades;

    @Data
    public static class GradeEntry {
        @NotNull
        private UUID studentId;
        private Double grade1;
        private Double grade2;
    }
}
