package com.edukira.enums;

public enum NotificationType {
    ATTENDANCE_ABSENCE,      // aluno ausente — avisa responsável
    ATTENDANCE_LATE,         // aluno atrasado
    GRADE_PUBLISHED,         // notas publicadas
    PAYMENT_CONFIRMED,       // pagamento confirmado
    PAYMENT_OVERDUE,         // mensalidade em atraso
    ENROLLMENT_APPROVED,     // matrícula aprovada
    ENROLLMENT_REJECTED,     // matrícula rejeitada
    ACCOUNT_APPROVED,        // conta de aluno/tutor aprovada
    MARKETPLACE_PURCHASE,    // compra no marketplace
    MARKETPLACE_SALE,        // venda realizada (avisa vendedor)
    WITHDRAWAL_PROCESSED,    // saque processado
    CUSTOM                   // mensagem personalizada do admin
}
