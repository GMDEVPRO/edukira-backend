package com.edukira.repository;

import com.edukira.entity.MarketplaceWithdrawal;
import com.edukira.enums.WithdrawalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface MarketplaceWithdrawalRepository extends JpaRepository<MarketplaceWithdrawal, UUID> {
    Page<MarketplaceWithdrawal> findBySellerId(UUID sellerId, Pageable pageable);
    boolean existsBySellerIdAndStatus(UUID sellerId, WithdrawalStatus status);
}
