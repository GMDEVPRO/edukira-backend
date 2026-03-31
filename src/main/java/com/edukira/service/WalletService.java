package com.edukira.service;

import com.edukira.dto.response.TransactionResponse;
import com.edukira.dto.response.WalletResponse;
import com.edukira.entity.Wallet;
import com.edukira.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface WalletService {

    /** Retorna ou cria carteira do seller autenticado */
    WalletResponse getMyWallet();

    /** Retorna ou cria carteira de um seller (chamado internamente) */
    Wallet getOrCreateWallet(UUID sellerId);

    /** Credita uma venda no wallet do seller e regista transacção */
    void credit(UUID sellerId, BigDecimal amount, String currency,
                UUID referenceId, String description);

    /** Debita um saque aprovado e regista transacção */
    void debit(UUID sellerId, BigDecimal amount, String currency,
               UUID referenceId, String description);

    /** Histórico de transacções do seller autenticado */
    Page<TransactionResponse> myTransactions(TransactionType type, Pageable pageable);
}
