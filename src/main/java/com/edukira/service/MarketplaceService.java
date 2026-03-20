package com.edukira.service;

import com.edukira.dto.request.*;
import com.edukira.dto.response.*;
import com.edukira.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface MarketplaceService {

    // ── Seller ──
    SellerResponse registerSeller(SellerRequest request);
    SellerResponse approveSeller(UUID sellerId);
    SellerResponse getMySeller();

    // ── Products ──
    ProductResponse createProduct(ProductRequest request);
    ProductResponse approveProduct(UUID productId);
    ProductResponse rejectProduct(UUID productId, String reason);
    Page<ProductResponse> listProducts(String category, String q, Pageable pageable);
    Page<ProductResponse> myProducts(Pageable pageable);
    ProductResponse getProduct(UUID productId);

    // ── Orders ──
    OrderResponse purchaseProduct(UUID productId, String paymentMethod);
    Page<OrderResponse> myPurchases(Pageable pageable);
    Page<OrderResponse> mySales(Pageable pageable);

    // ── Withdrawals ──
    WithdrawalResponse requestWithdrawal(WithdrawalRequest request);
    Page<WithdrawalResponse> myWithdrawals(Pageable pageable);
    SellerDashboardResponse sellerDashboard();
}
