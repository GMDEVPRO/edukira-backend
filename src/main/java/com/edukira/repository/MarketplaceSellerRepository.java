package com.edukira.repository;

import com.edukira.entity.MarketplaceSeller;
import com.edukira.enums.SellerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface MarketplaceSellerRepository extends JpaRepository<MarketplaceSeller, UUID> {
    Optional<MarketplaceSeller> findByUserProfileId(UUID userProfileId);
    boolean existsByUserProfileId(UUID userProfileId);
    long countByStatus(SellerStatus status);
}
