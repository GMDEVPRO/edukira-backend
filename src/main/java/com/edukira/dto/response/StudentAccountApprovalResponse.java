package com.edukira.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data @Builder
public class StudentAccountApprovalResponse {
    private String accountId;
    private String documentNumber;
    private String documentType;
    private String registeredBy;
    private String phone;
    private String email;
    private String status;
    private Instant createdAt;

    // Dados do aluno se já foi matched pela escola
    private String studentId;
    private String studentName;
    private String classLevel;
}
