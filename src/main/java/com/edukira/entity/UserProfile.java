package com.edukira.entity;

import com.edukira.enums.Language;
import com.edukira.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_profiles",
       uniqueConstraints = @UniqueConstraint(columnNames = {"email", "school_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_language", nullable = false)
    @Builder.Default
    private Language preferredLanguage = Language.fr;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "last_login")
    private Instant lastLogin;
}
