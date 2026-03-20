package com.edukira.repository;

import com.edukira.entity.MarketplaceOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.UUID;

public interface MarketplaceOrderRepository extends JpaRepository<MarketplaceOrder, UUID> {
    Page<MarketplaceOrder> findByBuyerSchoolId(UUID schoolId, Pageable pageable);
    Page<MarketplaceOrder> findByProductSellerId(UUID sellerId, Pageable pageable);
    boolean existsByProductIdAndBuyerSchoolId(UUID productId, UUID schoolId);

    @Query("SELECT COALESCE(SUM(o.sellerAmount),0) FROM MarketplaceOrder o " +
            "WHERE o.product.seller.id = :sellerId AND o.status = 'COMPLETED'")
    BigDecimal totalEarnedBySeller(UUID sellerId);
}
