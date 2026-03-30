package com.edukira.dto.response;

import com.edukira.enums.Language;
import com.edukira.enums.StudentStatus;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
@Getter
@Setter
@Data @Builder
public class StudentResponse {
    private UUID id;
    private UUID schoolId;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String gender;
    private String documentNumber;
    private String classLevel;
    private String guardianName;
    private String guardianPhone;
    private String guardianDocumentNumber;
    private String guardianEmail;
    private Language guardianLanguage;
    private StudentStatus status;
    private LocalDate enrollmentDate;
    private Instant createdAt;
    private Instant updatedAt;


}