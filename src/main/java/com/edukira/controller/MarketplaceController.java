package com.edukira.controller;

import com.edukira.dto.request.*;
import com.edukira.dto.response.*;
import com.edukira.service.MarketplaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/marketplace")
@RequiredArgsConstructor
public class MarketplaceController {

    private final MarketplaceService marketplaceService;

    // ════════════════════════════════
    // SELLER
    // ════════════════════════════════

    @PostMapping("/sellers/register")
    public ResponseEntity<ApiResponse<SellerResponse>> registerSeller(
            @Valid @RequestBody SellerRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                marketplaceService.registerSeller(request)));
    }

    @PostMapping("/sellers/{sellerId}/approve")
    public ResponseEntity<ApiResponse<SellerResponse>> approveSeller(
            @PathVariable UUID sellerId) {
        return ResponseEntity.ok(ApiResponse.ok(
                marketplaceService.approveSeller(sellerId)));
    }

    @GetMapping("/sellers/me")
    public ResponseEntity<ApiResponse<SellerResponse>> getMySeller() {
        return ResponseEntity.ok(ApiResponse.ok(
                marketplaceService.getMySeller()));
    }

    @GetMapping("/sellers/me/dashboard")
    public ResponseEntity<ApiResponse<SellerDashboardResponse>> sellerDashboard() {
        return ResponseEntity.ok(ApiResponse.ok(
                marketplaceService.sellerDashboard()));
    }

    // ════════════════════════════════
    // PRODUCTS
    // ════════════════════════════════

    @PostMapping("/products")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                marketplaceService.createProduct(request)));
    }

    @PostMapping("/products/{productId}/approve")
    public ResponseEntity<ApiResponse<ProductResponse>> approveProduct(
            @PathVariable UUID productId) {
        return ResponseEntity.ok(ApiResponse.ok(
                marketplaceService.approveProduct(productId)));
    }

    @PostMapping("/products/{productId}/reject")
    public ResponseEntity<ApiResponse<ProductResponse>> rejectProduct(
            @PathVariable UUID productId,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(ApiResponse.ok(
                marketplaceService.rejectProduct(productId, reason)));
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> listProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                marketplaceService.listProducts(category, q, pageable)));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(
            @PathVariable UUID productId) {
        return ResponseEntity.ok(ApiResponse.ok(
                marketplaceService.getProduct(productId)));
    }

    @GetMapping("/products/me")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> myProducts(
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                marketplaceService.myProducts(pageable)));
    }

    // ════════════════════════════════
    // ORDERS
    // ════════════════════════════════

    @PostMapping("/orders/{productId}/purchase")
    public ResponseEntity<ApiResponse<OrderResponse>> purchaseProduct(
            @PathVariable UUID productId,
            @RequestParam String paymentMethod) {
        return ResponseEntity.ok(ApiResponse.ok(
                marketplaceService.purchaseProduct(productId, paymentMethod)));
    }

    @GetMapping("/orders/purchases")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> myPurchases(
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                marketplaceService.myPurchases(pageable)));
    }

    @GetMapping("/orders/sales")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> mySales(
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                marketplaceService.mySales(pageable)));
    }

    // ════════════════════════════════
    // WITHDRAWALS
    // ════════════════════════════════

    @PostMapping("/withdrawals")
    public ResponseEntity<ApiResponse<WithdrawalResponse>> requestWithdrawal(
            @Valid @RequestBody WithdrawalRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                marketplaceService.requestWithdrawal(request)));
    }

    @GetMapping("/withdrawals")
    public ResponseEntity<ApiResponse<Page<WithdrawalResponse>>> myWithdrawals(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                marketplaceService.myWithdrawals(pageable)));
    }
}
