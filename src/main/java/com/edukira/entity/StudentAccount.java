package com.edukira.entity;

import com.edukira.enums.Language;
import com.edukira.enums.StudentAccountStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Conta de acesso criada pelo próprio aluno ou tutor.
 * Separada de UserProfile (que é conta interna da escola).
 * Após criação, fica PENDING_APPROVAL até admin aprovar.
 */
@Entity
@Table(name = "student_accounts",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"document_number", "school_id"}),
           @UniqueConstraint(columnNames = {"email"})
       })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Ligação à ficha do aluno (feita pelo admin após aprovação)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    // Dados de registo fornecidos pelo aluno/tutor
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "document_number", nullable = false)
    private String documentNumber;   // BI, Passaporte, Cédula Escolar

    @Column(name = "document_type")
    private String documentType;     // "BI", "PASSPORT", "CEDULA"

    // Contacto (editável pelo aluno)
    @Column
    private String email;

    @Column
    private String phone;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_language")
    @Builder.Default
    private Language preferredLanguage = Language.fr;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StudentAccountStatus status = StudentAccountStatus.PENDING_APPROVAL;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "approved_by")
    private UUID approvedBy;   // userId do admin que aprovou

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "last_login")
    private Instant lastLogin;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
