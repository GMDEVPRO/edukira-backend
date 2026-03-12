package com.edukira.dto.request;
import com.edukira.enums.Language;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;

@Data
public class StudentRequest {
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    private String classLevel;
    private String guardianName;
    private String guardianPhone;
    private String guardianEmail;
    private Language guardianLanguage;
    private LocalDate enrollmentDate;
}
