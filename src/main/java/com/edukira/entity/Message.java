package com.edukira.entity;

<<<<<<< HEAD
=======
import com.edukira.enums.Language;
>>>>>>> 94be4867219629388a5124e0c6675443891a295c
import com.edukira.enums.MessageChannel;
import com.edukira.enums.MessageStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "messages")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
<<<<<<< HEAD
    @JoinColumn(name = "sender_id", nullable = true)
    private UserProfile sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = true)
    private Student student;

=======
    @JoinColumn(name = "sender_id", nullable = false)
    private UserProfile sender;

>>>>>>> 94be4867219629388a5124e0c6675443891a295c
    @Column(name = "recipient_phone")
    private String recipientPhone;

    @Column(name = "recipient_email")
    private String recipientEmail;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
<<<<<<< HEAD
=======
    private Language language;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
>>>>>>> 94be4867219629388a5124e0c6675443891a295c
    @Builder.Default
    private MessageStatus status = MessageStatus.SENT;

    @Column(name = "external_message_id")
    private String externalMessageId;

    @CreationTimestamp
    @Column(name = "sent_at", updatable = false)
    private Instant sentAt;
}
