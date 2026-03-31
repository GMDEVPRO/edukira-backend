package com.edukira.repository;

import com.edukira.entity.Commission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface CommissionRepository extends JpaRepository<Commission, UUID> {

    Page<Commission> findBySellerId(UUID sellerId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(c.commissionAmount),0) FROM Commission c WHERE c.createdAt BETWEEN :from AND :to")
    BigDecimal totalCommissionBetween(Instant from, Instant to);

    @Query("SELECT COALESCE(SUM(c.commissionAmount),0) FROM Commission c WHERE c.seller.id = :sellerId")
    BigDecimal totalCommissionBySeller(UUID sellerId);

    @Query("SELECT COALESCE(SUM(c.commissionAmount),0) FROM Commission c")
    BigDecimal totalCommissionAll();
}
