package com.edukira.entity;

import com.edukira.enums.Language;
import com.edukira.enums.SchoolType;
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

    @Column(nullable = false, length = 3)
    private String country;   // SN, CI, ML...

    private String city;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SchoolType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_language", nullable = false)
    private Language defaultLanguage;

    // Mobile Money credentials (encrypted in prod via env vars)
    @Column(name = "wave_api_key")
    private String waveApiKey;

    @Column(name = "orange_api_key")
    private String orangeApiKey;

    @Column(name = "mtn_subscription_key")
    private String mtnSubscriptionKey;

    @Column(name = "africas_talking_key")
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
