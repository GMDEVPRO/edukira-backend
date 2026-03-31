package com.edukira.repository;

import com.edukira.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    Optional<Wallet> findBySellerId(UUID sellerId);
    boolean existsBySellerId(UUID sellerId);
}
