package com.edukira.repository;

import com.edukira.entity.Transaction;
import com.edukira.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Page<Transaction> findByWalletId(UUID walletId, Pageable pageable);
    Page<Transaction> findByWalletIdAndType(UUID walletId, TransactionType type, Pageable pageable);

    @Query("SELECT COALESCE(SUM(t.amount),0) FROM Transaction t WHERE t.wallet.id = :walletId AND t.type = 'CREDIT'")
    BigDecimal sumCreditsByWalletId(UUID walletId);

    @Query("SELECT COALESCE(SUM(t.amount),0) FROM Transaction t WHERE t.wallet.id = :walletId AND t.type = 'COMMISSION'")
    BigDecimal sumCommissionsByWalletId(UUID walletId);
}
