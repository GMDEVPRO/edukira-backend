package com.edukira.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data @Builder
public class StudentDocumentResponse {
    private String id;
    private String type;
    private String title;
    private String fileUrl;
    private String schoolYear;
    private Instant createdAt;
}
