package com.edukira.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LeadRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 200)
    private String name;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    @Size(max = 255)
    private String email;

    @NotBlank(message = "Nome da escola é obrigatório")
    @Size(max = 255)
    private String school;

    @Size(max = 50)
    private String phone;

    private String message;

    // idioma que o visitante estava usando na landing (fr, en, pt)
    private String language = "fr";
}
