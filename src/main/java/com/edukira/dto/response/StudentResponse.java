package com.edukira.dto.response;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data @Builder
public class StudentResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String classLevel;
    private String guardianName;
    private String guardianPhone;
    private String guardianLanguage;
    private String status;
    private LocalDate enrollmentDate;
}
