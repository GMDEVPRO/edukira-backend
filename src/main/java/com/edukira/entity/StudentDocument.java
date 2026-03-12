package com.edukira.entity;

import com.edukira.enums.DocumentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "student_documents")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType type;

    @Column(nullable = false)
    private String title;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "school_year")
    private String schoolYear;   // "2025-2026"

    @Column(nullable = false)
    @Builder.Default
    private Boolean visible = true;   // admin pode ocultar

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
