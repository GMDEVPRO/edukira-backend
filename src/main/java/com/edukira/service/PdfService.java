package com.edukira.service;

import com.edukira.entity.Payment;
import com.edukira.entity.Student;

import java.util.List;
import java.util.UUID;

public interface PdfService {

    /** Gera boletim PDF de um aluno para um período/ano */
    byte[] generateReportCard(UUID studentId, UUID schoolId, String period, String year);

    /** Gera recibo de pagamento PDF */
    byte[] generatePaymentReceipt(Payment payment);
}
