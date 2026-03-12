package com.edukira.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class StudentLoginRequest {

    @NotBlank(message = "Número de documento obrigatório")
    private String documentNumber;

    @NotBlank(message = "Password obrigatória")
    private String password;

    @NotNull(message = "ID da escola obrigatório")
    private UUID schoolId;
}
