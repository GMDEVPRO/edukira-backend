package com.edukira.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "school_rankings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"school_id", "year", "period"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SchoolRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(nullable = false, length = 3)
    private String countryCode;

    @Column(nullable = false)
    private String year;

    @Column(nullable = false)
    private String period;

    @Column(name = "academic_score")
    private BigDecimal academicScore;

    @Column(name = "attendance_score")
    private BigDecimal attendanceScore;

    @Column(name = "payment_score")
    private BigDecimal paymentScore;

    @Column(name = "global_score")
    private BigDecimal globalScore;

    @Column(name = "national_rank")
    private Integer nationalRank;

    @Column(name = "regional_rank")
    private Integer regionalRank;

    @Column(name = "total_students")
    private Integer totalStudents;

    @Column(name = "pass_rate")
    private BigDecimal passRate;

    @Column(name = "attendance_rate")
    private BigDecimal attendanceRate;

    @Column(name = "payment_rate")
    private BigDecimal paymentRate;

    @CreationTimestamp
    @Column(name = "computed_at", updatable = false)
    private Instant computedAt;
}