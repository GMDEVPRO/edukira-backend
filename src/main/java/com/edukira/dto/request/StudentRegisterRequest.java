package com.edukira.dto.request;

import com.edukira.enums.Language;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.UUID;

@Data
public class StudentRegisterRequest {

    @NotBlank(message = "Nome completo obrigatório")
    private String fullName;

    @NotBlank(message = "Número de documento obrigatório")
    private String documentNumber;

    @NotBlank(message = "Tipo de documento obrigatório")
    private String documentType;   // "BI", "PASSPORT", "CEDULA"

    @NotNull(message = "ID da escola obrigatório")
    private UUID schoolId;

    private String email;

    private String phone;

    @NotBlank(message = "Password obrigatória")
    @Size(min = 6, message = "Password deve ter pelo menos 6 caracteres")
    private String password;

    private Language preferredLanguage;
}
