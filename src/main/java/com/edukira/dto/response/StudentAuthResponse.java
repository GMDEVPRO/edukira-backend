package com.edukira.dto.response;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class StudentAuthResponse {
    private String accessToken;
    private String refreshToken;
    private String studentAccountId;
    private String fullName;
    private String accountStatus;   // PENDING_APPROVAL, ACTIVE, etc.
    private String preferredLanguage;
    private SchoolInfo school;
    private Boolean isLinked;       // true se já ligado à ficha do aluno

    @Data @Builder
    public static class SchoolInfo {
        private String id;
        private String name;
        private String country;
    }
}
