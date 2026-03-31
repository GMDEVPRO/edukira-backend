package com.edukira.service.impl;

import com.edukira.dto.response.TransactionResponse;
import com.edukira.dto.response.WalletResponse;
import com.edukira.entity.MarketplaceSeller;
import com.edukira.entity.Transaction;
import com.edukira.entity.Wallet;
import com.edukira.enums.TransactionType;
import com.edukira.exception.EdukiraException;
import com.edukira.repository.MarketplaceSellerRepository;
import com.edukira.repository.TransactionRepository;
import com.edukira.repository.WalletRepository;
import com.edukira.security.SchoolContext;
import com.edukira.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final WalletRepository           walletRepo;
    private final TransactionRepository      transactionRepo;
    private final MarketplaceSellerRepository sellerRepo;

    @Override
    public WalletResponse getMyWallet() {
        UUID userId = SchoolContext.getUserId();
        MarketplaceSeller seller = sellerRepo.findByUserProfileId(userId)
                .orElseThrow(() -> EdukiraException.notFound("Perfil de vendedor"));
        Wallet wallet = getOrCreateWallet(seller.getId());
        return toResponse(wallet);
    }

    @Override
    @Transactional
    public Wallet getOrCreateWallet(UUID sellerId) {
        return walletRepo.findBySellerId(sellerId).orElseGet(() -> {
            MarketplaceSeller seller = sellerRepo.findById(sellerId)
                    .orElseThrow(() -> EdukiraException.notFound("Vendedor"));
            Wallet w = Wallet.builder()
                    .seller(seller)
                    .availableBalance(BigDecimal.ZERO)
                    .pendingBalance(BigDecimal.ZERO)
                    .totalEarned(BigDecimal.ZERO)
                    .totalWithdrawn(BigDecimal.ZERO)
                    .currency("XOF")
                    .build();
            log.info("[WALLET] Carteira criada para seller={}", sellerId);
            return walletRepo.save(w);
        });
    }

    @Override
    @Transactional
    public void credit(UUID sellerId, BigDecimal amount, String currency,
                       UUID referenceId, String description) {
        Wallet wallet = getOrCreateWallet(sellerId);

        wallet.setAvailableBalance(wallet.getAvailableBalance().add(amount));
        wallet.setTotalEarned(wallet.getTotalEarned().add(amount));
        walletRepo.save(wallet);

        Transaction tx = Transaction.builder()
                .wallet(wallet)
                .type(TransactionType.CREDIT)
                .amount(amount)
                .balanceAfter(wallet.getAvailableBalance())
                .currency(currency != null ? currency : "XOF")
                .referenceId(referenceId)
                .referenceType("ORDER")
                .description(description)
                .build();
        transactionRepo.save(tx);

        log.info("[WALLET] Crédito | seller={} valor={} ref={}", sellerId, amount, referenceId);
    }

    @Override
    @Transactional
    public void debit(UUID sellerId, BigDecimal amount, String currency,
                      UUID referenceId, String description) {
        Wallet wallet = getOrCreateWallet(sellerId);

        if (wallet.getAvailableBalance().compareTo(amount) < 0) {
            throw EdukiraException.badRequest("Saldo insuficiente para saque");
        }

        wallet.setAvailableBalance(wallet.getAvailableBalance().subtract(amount));
        wallet.setTotalWithdrawn(wallet.getTotalWithdrawn().add(amount));
        walletRepo.save(wallet);

        Transaction tx = Transaction.builder()
                .wallet(wallet)
                .type(TransactionType.WITHDRAWAL)
                .amount(amount)
                .balanceAfter(wallet.getAvailableBalance())
                .currency(currency != null ? currency : "XOF")
                .referenceId(referenceId)
                .referenceType("WITHDRAWAL")
                .description(description)
                .build();
        transactionRepo.save(tx);

        log.info("[WALLET] Débito | seller={} valor={} ref={}", sellerId, amount, referenceId);
    }

    @Override
    public Page<TransactionResponse> myTransactions(TransactionType type, Pageable pageable) {
        UUID userId = SchoolContext.getUserId();
        MarketplaceSeller seller = sellerRepo.findByUserProfileId(userId)
                .orElseThrow(() -> EdukiraException.notFound("Perfil de vendedor"));
        Wallet wallet = getOrCreateWallet(seller.getId());

        Page<Transaction> page = (type != null)
                ? transactionRepo.findByWalletIdAndType(wallet.getId(), type, pageable)
                : transactionRepo.findByWalletId(wallet.getId(), pageable);

        return page.map(this::toTxResponse);
    }

    // ── mappers ──────────────────────────────────────────

    private WalletResponse toResponse(Wallet w) {
        return WalletResponse.builder()
                .id(w.getId())
                .availableBalance(w.getAvailableBalance())
                .pendingBalance(w.getPendingBalance())
                .totalEarned(w.getTotalEarned())
                .totalWithdrawn(w.getTotalWithdrawn())
                .currency(w.getCurrency())
                .updatedAt(w.getUpdatedAt())
                .build();
    }

    private TransactionResponse toTxResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .type(t.getType())
                .amount(t.getAmount())
                .balanceAfter(t.getBalanceAfter())
                .currency(t.getCurrency())
                .referenceId(t.getReferenceId())
                .referenceType(t.getReferenceType())
                .description(t.getDescription())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
