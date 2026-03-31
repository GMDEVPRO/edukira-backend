package com.edukira.service.impl;

import com.edukira.dto.response.NotificationLogResponse;
import com.edukira.dto.response.NotificationSummaryResponse;
import com.edukira.entity.NotificationLog;
import com.edukira.entity.School;
import com.edukira.enums.NotificationChannel;
import com.edukira.enums.NotificationStatus;
import com.edukira.enums.NotificationType;
import com.edukira.repository.NotificationLogRepository;
import com.edukira.repository.SchoolRepository;
import com.edukira.service.NotificationService;
import com.edukira.service.SmsService;
import com.edukira.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationLogRepository notifRepo;
    private final SchoolRepository          schoolRepo;
    private final SmsService                smsService;
    private final WhatsAppService           whatsAppService;

    // ── Helpers internos ─────────────────────────────────────────────

    /**
     * Envia de forma assíncrona (fora da transação JPA principal)
     * e persiste o log com status SENT ou FAILED.
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendAndLog(UUID schoolId, NotificationType type,
                           NotificationChannel channel, String recipientPhone,
                           String recipientName, UUID entityId, String entityType,
                           String body) {
        School school = schoolRepo.findById(schoolId).orElse(null);

        NotificationLog log2 = NotificationLog.builder()
                .school(school)
                .type(type)
                .channel(channel)
                .status(NotificationStatus.PENDING)
                .recipientPhone(recipientPhone)
                .recipientName(recipientName)
                .entityId(entityId)
                .entityType(entityType)
                .body(body)
                .build();

        notifRepo.save(log2);

        try {
            String externalId = switch (channel) {
                case WHATSAPP -> whatsAppService.send(recipientPhone, body);
                case SMS      -> smsService.send(recipientPhone, body);
                default       -> null;
            };

            log2.setStatus(NotificationStatus.SENT);
            log2.setExternalId(externalId);
            log2.setSentAt(Instant.now());
            log.info("[NOTIFICATION] Enviada | tipo={} canal={} para={}", type, channel, recipientPhone);

        } catch (Exception e) {
            log2.setStatus(NotificationStatus.FAILED);
            log2.setErrorMessage(e.getMessage());
            log.error("[NOTIFICATION] Falhou | tipo={} canal={} erro={}", type, channel, e.getMessage());
        }

        notifRepo.save(log2);
    }

    // ── Notificações específicas ──────────────────────────────────────

    @Override
    public void notifyAbsence(UUID schoolId, UUID studentId, String studentName,
                               String guardianPhone, String guardianName,
                               String date, String subject) {
        if (guardianPhone == null || guardianPhone.isBlank()) return;
        String body = String.format(
                "Bonjour %s, votre enfant %s était ABSENT(e) le %s (%s). Edukira.",
                guardianName, studentName, date, subject != null ? subject : "cours");
        sendAndLog(schoolId, NotificationType.ATTENDANCE_ABSENCE, NotificationChannel.WHATSAPP,
                guardianPhone, guardianName, studentId, "STUDENT", body);
    }

    @Override
    public void notifyLate(UUID schoolId, UUID studentId, String studentName,
                            String guardianPhone, String guardianName, String date) {
        if (guardianPhone == null || guardianPhone.isBlank()) return;
        String body = String.format(
                "Bonjour %s, votre enfant %s est arrivé EN RETARD le %s. Edukira.",
                guardianName, studentName, date);
        sendAndLog(schoolId, NotificationType.ATTENDANCE_LATE, NotificationChannel.WHATSAPP,
                guardianPhone, guardianName, studentId, "STUDENT", body);
    }

    @Override
    public void notifyGradePublished(UUID schoolId, UUID studentId, String studentName,
                                      String guardianPhone, String guardianName,
                                      String subject, String period) {
        if (guardianPhone == null || guardianPhone.isBlank()) return;
        String body = String.format(
                "Bonjour %s, les notes de %s en %s (%s) sont disponibles sur Edukira.",
                guardianName, studentName, subject, period);
        sendAndLog(schoolId, NotificationType.GRADE_PUBLISHED, NotificationChannel.WHATSAPP,
                guardianPhone, guardianName, studentId, "STUDENT", body);
    }

    @Override
    public void notifyPaymentConfirmed(UUID schoolId, UUID studentId, String studentName,
                                        String guardianPhone, String guardianName,
                                        String amount, String currency, String month) {
        if (guardianPhone == null || guardianPhone.isBlank()) return;
        String body = String.format(
                "Paiement confirmé pour %s — %s %s (mois: %s). Merci! Edukira.",
                studentName, amount, currency, month);
        sendAndLog(schoolId, NotificationType.PAYMENT_CONFIRMED, NotificationChannel.WHATSAPP,
                guardianPhone, guardianName, studentId, "STUDENT", body);
    }

    @Override
    public void notifyPaymentOverdue(UUID schoolId, UUID studentId, String studentName,
                                      String guardianPhone, String guardianName,
                                      String amount, String currency, String month) {
        if (guardianPhone == null || guardianPhone.isBlank()) return;
        String body = String.format(
                "RAPPEL: Le paiement de %s %s pour %s (mois: %s) est en retard. Edukira.",
                amount, currency, studentName, month);
        sendAndLog(schoolId, NotificationType.PAYMENT_OVERDUE, NotificationChannel.SMS,
                guardianPhone, guardianName, studentId, "STUDENT", body);
    }

    @Override
    public void notifyEnrollmentApproved(UUID schoolId, String recipientPhone,
                                          String recipientName, String schoolName) {
        if (recipientPhone == null || recipientPhone.isBlank()) return;
        String body = String.format(
                "Bonjour %s, votre inscription à %s a été approuvée! Bienvenue sur Edukira.",
                recipientName, schoolName);
        sendAndLog(schoolId, NotificationType.ENROLLMENT_APPROVED, NotificationChannel.WHATSAPP,
                recipientPhone, recipientName, null, "ENROLLMENT", body);
    }

    @Override
    public void notifyAccountApproved(UUID schoolId, String recipientPhone, String recipientName) {
        if (recipientPhone == null || recipientPhone.isBlank()) return;
        String body = String.format(
                "Bonjour %s, votre compte Edukira a été approuvé! Vous pouvez maintenant vous connecter.",
                recipientName);
        sendAndLog(schoolId, NotificationType.ACCOUNT_APPROVED, NotificationChannel.WHATSAPP,
                recipientPhone, recipientName, null, "ACCOUNT", body);
    }

    @Override
    public void notifyMarketplaceSale(UUID schoolId, String sellerPhone, String sellerName,
                                       String productTitle, String amount, String currency) {
        if (sellerPhone == null || sellerPhone.isBlank()) return;
        String body = String.format(
                "Bonne nouvelle %s! Votre produit '%s' a été vendu pour %s %s. Edukira Marketplace.",
                sellerName, productTitle, amount, currency);
        sendAndLog(schoolId, NotificationType.MARKETPLACE_SALE, NotificationChannel.WHATSAPP,
                sellerPhone, sellerName, null, "SELLER", body);
    }

    @Override
    public void notifyWithdrawalProcessed(UUID schoolId, String sellerPhone, String sellerName,
                                           String amount, String currency) {
        if (sellerPhone == null || sellerPhone.isBlank()) return;
        String body = String.format(
                "Bonjour %s, votre retrait de %s %s a été traité et envoyé. Edukira.",
                sellerName, amount, currency);
        sendAndLog(schoolId, NotificationType.WITHDRAWAL_PROCESSED, NotificationChannel.SMS,
                sellerPhone, sellerName, null, "SELLER", body);
    }

    // ── Histórico e admin ─────────────────────────────────────────────

    @Override
    public Page<NotificationLogResponse> getHistory(UUID schoolId, NotificationType type,
                                                     NotificationChannel channel,
                                                     NotificationStatus status,
                                                     Pageable pageable) {
        Page<NotificationLog> page;
        if (type != null) {
            page = notifRepo.findBySchoolIdAndType(schoolId, type, pageable);
        } else if (status != null) {
            page = notifRepo.findBySchoolIdAndStatus(schoolId, status, pageable);
        } else if (channel != null) {
            page = notifRepo.findBySchoolIdAndChannel(schoolId, channel, pageable);
        } else {
            page = notifRepo.findBySchoolId(schoolId, pageable);
        }
        return page.map(this::toResponse);
    }

    @Override
    public NotificationSummaryResponse getSummary(UUID schoolId) {
        return NotificationSummaryResponse.builder()
                .totalSent(notifRepo.countBySchoolIdAndStatus(schoolId, NotificationStatus.SENT))
                .totalFailed(notifRepo.countBySchoolIdAndStatus(schoolId, NotificationStatus.FAILED))
                .totalPending(notifRepo.countBySchoolIdAndStatus(schoolId, NotificationStatus.PENDING))
                .absenceAlerts(notifRepo.countBySchoolIdAndType(schoolId, NotificationType.ATTENDANCE_ABSENCE))
                .gradeAlerts(notifRepo.countBySchoolIdAndType(schoolId, NotificationType.GRADE_PUBLISHED))
                .paymentAlerts(notifRepo.countBySchoolIdAndType(schoolId, NotificationType.PAYMENT_CONFIRMED)
                        + notifRepo.countBySchoolIdAndType(schoolId, NotificationType.PAYMENT_OVERDUE))
                .build();
    }

    @Override
    @Transactional
    public int retryFailed() {
        List<NotificationLog> retryable = notifRepo.findRetryable();
        int count = 0;
        for (NotificationLog n : retryable) {
            try {
                String externalId = switch (n.getChannel()) {
                    case WHATSAPP -> whatsAppService.send(n.getRecipientPhone(), n.getBody());
                    case SMS      -> smsService.send(n.getRecipientPhone(), n.getBody());
                    default       -> null;
                };
                n.setStatus(NotificationStatus.SENT);
                n.setExternalId(externalId);
                n.setSentAt(Instant.now());
                count++;
            } catch (Exception e) {
                n.setRetryCount(n.getRetryCount() + 1);
                n.setErrorMessage(e.getMessage());
                if (n.getRetryCount() >= 3) {
                    n.setStatus(NotificationStatus.FAILED);
                }
            }
            notifRepo.save(n);
        }
        log.info("[NOTIFICATION] Retry: {} enviadas de {} tentadas", count, retryable.size());
        return count;
    }

    private NotificationLogResponse toResponse(NotificationLog n) {
        return NotificationLogResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .channel(n.getChannel())
                .status(n.getStatus())
                .recipientName(n.getRecipientName())
                .recipientPhone(n.getRecipientPhone())
                .body(n.getBody())
                .externalId(n.getExternalId())
                .errorMessage(n.getErrorMessage())
                .retryCount(n.getRetryCount())
                .sentAt(n.getSentAt())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
