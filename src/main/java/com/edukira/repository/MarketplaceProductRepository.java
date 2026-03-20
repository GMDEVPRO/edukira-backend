package com.edukira.repository;

import com.edukira.entity.MarketplaceProduct;
import com.edukira.enums.ProductCategory;
import com.edukira.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.UUID;

public interface MarketplaceProductRepository extends JpaRepository<MarketplaceProduct, UUID> {
    Page<MarketplaceProduct> findByStatus(ProductStatus status, Pageable pageable);
    Page<MarketplaceProduct> findByStatusAndCategory(ProductStatus status, ProductCategory category, Pageable pageable);
    Page<MarketplaceProduct> findBySellerId(UUID sellerId, Pageable pageable);

    @Query("SELECT p FROM MarketplaceProduct p WHERE p.status = 'APPROVED' AND " +
            "(LOWER(p.title) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<MarketplaceProduct> search(String q, Pageable pageable);
}
