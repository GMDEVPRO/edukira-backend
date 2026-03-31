package com.edukira.enums;

public enum TransactionType {
    CREDIT,       // crédito por venda
    DEBIT,        // débito por saque aprovado
    WITHDRAWAL,   // saque solicitado (debita do available, move para withdrawn)
    COMMISSION,   // comissão Edukira registada
    REFUND        // reembolso futuro
}
