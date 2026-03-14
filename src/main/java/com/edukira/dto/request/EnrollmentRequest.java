package com.edukira.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EnrollmentRequest {

    @NotBlank
    private String studentFirstName;
    @NotBlank
    private String studentLastName;
    private String studentBirthDate;
    private String studentGender;
    private String studentNationality;

    @NotBlank
    private String classLevel;
    private String previousSchool;
    private BigDecimal previousAverage;
    private String preferredLanguage;

    @NotBlank
    private String guardianName;
    @NotBlank
    private String guardianPhone;
    private String guardianWhatsapp;
    private String guardianEmail;
    private String guardianProfession;

    @NotNull
    private String paymentMethod;
    private BigDecimal paymentAmount;
}
