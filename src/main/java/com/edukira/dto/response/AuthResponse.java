package com.edukira.dto.response;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String role;
    private String preferredLanguage;
    private SchoolSummary school;

    @Data @Builder
    public static class SchoolSummary {
        private String id;
        private String name;
        private String country;
        private String type;
    }
}
