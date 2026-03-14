package com.edukira.entity;

import com.edukira.enums.EnrollmentStatus;
import com.edukira.enums.MessageChannel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "enrollments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(name = "student_first_name", nullable = false)
    private String studentFirstName;

    @Column(name = "student_last_name", nullable = false)
    private String studentLastName;

    @Column(name = "student_birth_date")
    private String studentBirthDate;

    @Column(name = "student_gender")
    private String studentGender;

    @Column(name = "student_nationality")
    private String studentNationality;

    @Column(name = "class_level", nullable = false)
    private String classLevel;

    @Column(name = "previous_school")
    private String previousSchool;

    @Column(name = "previous_average")
    private BigDecimal previousAverage;

    @Column(name = "guardian_name", nullable = false)
    private String guardianName;

    @Column(name = "guardian_phone", nullable = false)
    private String guardianPhone;

    @Column(name = "guardian_whatsapp")
    private String guardianWhatsapp;

    @Column(name = "guardian_email")
    private String guardianEmail;

    @Column(name = "guardian_profession")
    private String guardianProfession;

    @Column(name = "preferred_language")
    private String preferredLanguage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EnrollmentStatus status = EnrollmentStatus.PENDING;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "payment_amount")
    private BigDecimal paymentAmount;

    @Column(name = "payment_confirmed")
    @Builder.Default
    private boolean paymentConfirmed = false;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by", nullable = true)
    private UserProfile reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = true)
    private Student student;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
