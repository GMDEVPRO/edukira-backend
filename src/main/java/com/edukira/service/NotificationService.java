package com.edukira.service;

import com.edukira.dto.response.NotificationLogResponse;
import com.edukira.dto.response.NotificationSummaryResponse;
import com.edukira.enums.NotificationChannel;
import com.edukira.enums.NotificationStatus;
import com.edukira.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {

    // ── Envio de notificações específicas ──────────────────────────────

    /** Alerta de ausência ao responsável */
    void notifyAbsence(UUID schoolId, UUID studentId, String studentName,
                       String guardianPhone, String guardianName, String date, String subject);

    /** Alerta de atraso ao responsável */
    void notifyLate(UUID schoolId, UUID studentId, String studentName,
                    String guardianPhone, String guardianName, String date);

    /** Notas publicadas — avisa responsável */
    void notifyGradePublished(UUID schoolId, UUID studentId, String studentName,
                               String guardianPhone, String guardianName,
                               String subject, String period);

    /** Pagamento confirmado — avisa responsável */
    void notifyPaymentConfirmed(UUID schoolId, UUID studentId, String studentName,
                                 String guardianPhone, String guardianName,
                                 String amount, String currency, String month);

    /** Mensalidade em atraso */
    void notifyPaymentOverdue(UUID schoolId, UUID studentId, String studentName,
                               String guardianPhone, String guardianName,
                               String amount, String currency, String month);

    /** Matrícula aprovada */
    void notifyEnrollmentApproved(UUID schoolId, String recipientPhone,
                                   String recipientName, String schoolName);

    /** Conta de aluno/tutor aprovada */
    void notifyAccountApproved(UUID schoolId, String recipientPhone, String recipientName);

    /** Venda no marketplace — avisa vendedor */
    void notifyMarketplaceSale(UUID schoolId, String sellerPhone, String sellerName,
                                String productTitle, String amount, String currency);

    /** Saque processado — avisa vendedor */
    void notifyWithdrawalProcessed(UUID schoolId, String sellerPhone, String sellerName,
                                    String amount, String currency);

    // ── Histórico e administração ──────────────────────────────────────

    Page<NotificationLogResponse> getHistory(UUID schoolId, NotificationType type,
                                              NotificationChannel channel,
                                              NotificationStatus status,
                                              Pageable pageable);

    NotificationSummaryResponse getSummary(UUID schoolId);

    /** Retry manual de notificações falhadas */
    int retryFailed();
}
