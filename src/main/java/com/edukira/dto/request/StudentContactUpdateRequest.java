package com.edukira.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class StudentContactUpdateRequest {
    @Email(message = "Email inválido")
    private String email;
    private String phone;
}
