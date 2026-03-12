package com.edukira.entity;

import com.edukira.enums.GradePeriod;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "grades")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(name = "subject_name", nullable = false)
    private String subjectName;

    @Column(precision = 5, scale = 2)
    private BigDecimal grade1;

    @Column(precision = 5, scale = 2)
    private BigDecimal grade2;

    @Column(precision = 5, scale = 2)
    private BigDecimal average;

    @Column(precision = 5, scale = 2)
    private BigDecimal coefficient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GradePeriod period;

    @Column(nullable = false)
    private String year;   // "2025-2026"

    @Column(nullable = false)
    @Builder.Default
    private Boolean published = false;

    @Column(name = "published_at")
    private Instant publishedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
