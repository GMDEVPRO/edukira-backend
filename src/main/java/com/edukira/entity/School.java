package com.edukira.entity;

import com.edukira.enums.Language;
import com.edukira.enums.SchoolType;
import com.edukira.util.AesEncryptor;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "schools")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 50)
    private String country;   // SN, CI, ML...

    private String city;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SchoolType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_language", nullable = false)
    private Language defaultLanguage;

    // Mobile Money credentials (encrypted in prod via env vars)
    @Convert(converter = AesEncryptor.class)
    private String waveApiKey;

    @Convert(converter = AesEncryptor.class)
    private String orangeApiKey;

    @Convert(converter = AesEncryptor.class)
    private String mtnSubscriptionKey;

    @Convert(converter = AesEncryptor.class)
    private String africasTalkingKey;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
