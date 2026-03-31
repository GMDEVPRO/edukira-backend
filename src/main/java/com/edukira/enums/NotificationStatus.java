package com.edukira.enums;

public enum NotificationStatus {
    PENDING,   // na fila, aguarda envio
    SENT,      // enviado com sucesso
    FAILED,    // falha definitiva
    RETRYING   // em retry
}
