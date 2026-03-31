package com.edukira.service.impl;

import com.edukira.dto.response.CommissionResponse;
import com.edukira.dto.response.CommissionSummaryResponse;
import com.edukira.entity.Commission;
import com.edukira.entity.MarketplaceOrder;
import com.edukira.entity.MarketplaceSeller;
import com.edukira.exception.EdukiraException;
import com.edukira.repository.CommissionRepository;
import com.edukira.repository.MarketplaceSellerRepository;
import com.edukira.security.SchoolContext;
import com.edukira.service.CommissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommissionServiceImpl implements CommissionService {

    private final CommissionRepository       commissionRepo;
    private final MarketplaceSellerRepository sellerRepo;

    @Override
    @Transactional
    public Commission record(MarketplaceOrder order) {
        Commission commission = Commission.builder()
                .order(order)
                .seller(order.getProduct().getSeller())
                .saleAmount(order.getAmountPaid())
                .commissionRate(order.getCommissionRate())
                .commissionAmount(order.getCommissionAmt())
                .sellerNet(order.getSellerAmount())
                .currency(order.getCurrency())
                .productTitle(order.getProduct().getTitle())
                .sellerDisplayName(order.getProduct().getSeller().getDisplayName())
                .build();

        commissionRepo.save(commission);
        log.info("[COMMISSION] Registada | order={} valor={} taxa={}%",
                order.getId(), order.getCommissionAmt(), order.getCommissionRate());
        return commission;
    }

    @Override
    public CommissionSummaryResponse summary() {
        Instant startOfMonth = Instant.now().truncatedTo(ChronoUnit.DAYS)
                .minus(Instant.now().atZone(java.time.ZoneOffset.UTC).getDayOfMonth() - 1L, ChronoUnit.DAYS);

        BigDecimal allTime    = commissionRepo.totalCommissionAll();
        BigDecimal thisMonth  = commissionRepo.totalCommissionBetween(startOfMonth, Instant.now());
        long       total      = commissionRepo.count();

        return CommissionSummaryResponse.builder()
                .totalCommissionAllTime(allTime)
                .totalCommissionThisMonth(thisMonth)
                .totalOrders(total)
                .build();
    }

    @Override
    public Page<CommissionResponse> listAll(Pageable pageable) {
        return commissionRepo.findAll(pageable).map(this::toResponse);
    }

    @Override
    public Page<CommissionResponse> mySeller(Pageable pageable) {
        UUID userId = SchoolContext.getUserId();
        MarketplaceSeller seller = sellerRepo.findByUserProfileId(userId)
                .orElseThrow(() -> EdukiraException.notFound("Perfil de vendedor"));
        return commissionRepo.findBySellerId(seller.getId(), pageable).map(this::toResponse);
    }

    private CommissionResponse toResponse(Commission c) {
        return CommissionResponse.builder()
                .id(c.getId())
                .orderId(c.getOrder().getId())
                .productTitle(c.getProductTitle())
                .sellerDisplayName(c.getSellerDisplayName())
                .saleAmount(c.getSaleAmount())
                .commissionRate(c.getCommissionRate())
                .commissionAmount(c.getCommissionAmount())
                .sellerNet(c.getSellerNet())
                .currency(c.getCurrency())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
