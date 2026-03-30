package com.edukira.dto.request;

import com.edukira.enums.Language;
import com.edukira.enums.SchoolType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SchoolRegistrationRequest {

    @NotBlank
    private String schoolName;

    @NotNull
    private SchoolType schoolType;

    @NotBlank
    private String country;

    @NotBlank
    private String city;

    @NotBlank
    private String phone;

    @NotBlank
    @Email
    private String schoolEmail;

    private String website;
    private String estimatedStudents;
    private Language defaultLanguage;

    @NotBlank
    private String adminFirstName;

    @NotBlank
    private String adminLastName;

    @NotBlank
    private String adminRole;

    @NotBlank
    private String adminPhone;

    @NotBlank
    @Email
    private String adminEmail;

    @NotBlank
    private String adminPassword;

    // Documento de identidade do responsável
    private String adminIdType;   // NIF, BI, PASSPORT, CNIB, etc.
    private String adminIdNumber;

    @NotBlank
    private String plan;

    private String paymentMethod;
}