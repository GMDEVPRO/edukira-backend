package com.edukira.service.impl;

import com.edukira.dto.request.*;
import com.edukira.dto.response.*;
import com.edukira.entity.*;
import com.edukira.enums.*;
import com.edukira.repository.*;
import com.edukira.security.SchoolContext;
import com.edukira.service.MarketplaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketplaceServiceImpl implements MarketplaceService {

    private final MarketplaceSellerRepository    sellerRepo;
    private final MarketplaceProductRepository   productRepo;
    private final MarketplaceOrderRepository     orderRepo;
    private final MarketplaceWithdrawalRepository withdrawalRepo;
    private final UserProfileRepository          userProfileRepo;


    // ════════════════════════════════
    // SELLER
    // ════════════════════════════════

    @Override
    @Transactional
    public SellerResponse registerSeller(SellerRequest req) {
        UUID userId   = SchoolContext.getUserId();
        if (sellerRepo.existsByUserProfileId(userId)) {
            throw new RuntimeException("Vous êtes déjà enregistré comme vendeur.");
        }
        UserProfile user = userProfileRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        BigDecimal commission = switch (req.getSellerType()) {
            case TEACHER         -> new BigDecimal("20.00");
            case SCHOOL          -> new BigDecimal("15.00");
            case PUBLISHER       -> new BigDecimal("15.00");
            case EDUKIRA_PARTNER -> new BigDecimal("13.00");
        };

        MarketplaceSeller seller = MarketplaceSeller.builder()
                .userProfile(user)
                .displayName(req.getDisplayName())
                .bio(req.getBio())
                .sellerType(req.getSellerType())
                .commissionRate(commission)
                .mobileMoneyNumber(req.getMobileMoneyNumber())
                .mobileMoneyOperator(req.getMobileMoneyOperator())
                .status(SellerStatus.PENDING)
                .build();

        sellerRepo.save(seller);
        log.info("[MARKETPLACE] Nouveau vendeur enregistré | id={} type={}", seller.getId(), seller.getSellerType());
        return toSellerResponse(seller);
    }

    @Override
    @Transactional
    public SellerResponse approveSeller(UUID sellerId) {
        MarketplaceSeller seller = sellerRepo.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Vendeur introuvable"));
        seller.setStatus(SellerStatus.APPROVED);
        seller.setApprovedAt(Instant.now());
        sellerRepo.save(seller);
        log.info("[MARKETPLACE] Vendeur approuvé | id={}", sellerId);
        return toSellerResponse(seller);
    }

    @Override
    public SellerResponse getMySeller() {
        UUID userId   = SchoolContext.getUserId();
        MarketplaceSeller seller = sellerRepo.findByUserProfileId(userId)
                .orElseThrow(() -> new RuntimeException("Profil vendeur introuvable"));
        return toSellerResponse(seller);
    }

    // ════════════════════════════════
    // PRODUCTS
    // ════════════════════════════════

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest req) {
        UUID userId   = SchoolContext.getUserId();
        MarketplaceSeller seller = sellerRepo.findByUserProfileId(userId)
                .orElseThrow(() -> new RuntimeException("Profil vendeur introuvable"));

        if (seller.getStatus() != SellerStatus.APPROVED) {
            throw new RuntimeException("Votre compte vendeur n'est pas encore approuvé.");
        }

        MarketplaceProduct product = MarketplaceProduct.builder()
                .seller(seller)
                .title(req.getTitle())
                .description(req.getDescription())
                .category(req.getCategory())
                .subject(req.getSubject())
                .level(req.getLevel())
                .language(req.getLanguage() != null ? req.getLanguage() : Language.fr)
                .price(req.getPrice())
                .currency(req.getCurrency() != null ? req.getCurrency() : "XOF")
                .fileUrl(req.getFileUrl())
                .previewUrl(req.getPreviewUrl())
                .thumbnailUrl(req.getThumbnailUrl())
                .pages(req.getPages())
                .status(ProductStatus.PENDING)
                .build();

        productRepo.save(product);
        log.info("[MARKETPLACE] Nouveau produit soumis | id={} titre={}", product.getId(), product.getTitle());
        return toProductResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse approveProduct(UUID productId) {
        MarketplaceProduct product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));
        product.setStatus(ProductStatus.APPROVED);
        product.setApprovedAt(Instant.now());
        productRepo.save(product);
        return toProductResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse rejectProduct(UUID productId, String reason) {
        MarketplaceProduct product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));
        product.setStatus(ProductStatus.REJECTED);
        productRepo.save(product);
        return toProductResponse(product);
    }

    @Override
    public Page<ProductResponse> listProducts(String category, String q, Pageable pageable) {
        if (q != null && !q.isBlank()) {
            return productRepo.search(q, pageable).map(this::toProductResponse);
        }
        if (category != null && !category.isBlank()) {
            ProductCategory cat = ProductCategory.valueOf(category.toUpperCase());
            return productRepo.findByStatusAndCategory(ProductStatus.APPROVED, cat, pageable)
                    .map(this::toProductResponse);
        }
        return productRepo.findByStatus(ProductStatus.APPROVED, pageable)
                .map(this::toProductResponse);
    }

    @Override
    public Page<ProductResponse> myProducts(Pageable pageable) {
        UUID userId   = SchoolContext.getUserId();
        MarketplaceSeller seller = sellerRepo.findByUserProfileId(userId)
                .orElseThrow(() -> new RuntimeException("Profil vendeur introuvable"));
        return productRepo.findBySellerId(seller.getId(), pageable)
                .map(this::toProductResponse);
    }

    @Override
    public ProductResponse getProduct(UUID productId) {
        return toProductResponse(productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produit introuvable")));
    }

    // ════════════════════════════════
    // ORDERS
    // ════════════════════════════════

    @Override
    @Transactional
    public OrderResponse purchaseProduct(UUID productId, String paymentMethod) {
        UUID userId   = SchoolContext.getUserId();
        UUID schoolId = SchoolContext.getSchoolId();

        if (orderRepo.existsByProductIdAndBuyerSchoolId(productId, schoolId)) {
            throw new RuntimeException("Vous avez déjà acheté ce produit.");
        }

        MarketplaceProduct product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));

        if (product.getStatus() != ProductStatus.APPROVED) {
            throw new RuntimeException("Ce produit n'est pas disponible.");
        }

        BigDecimal price      = product.getPrice();
        BigDecimal commRate   = product.getSeller().getCommissionRate();
        BigDecimal commAmt    = price.multiply(commRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal sellerAmt  = price.subtract(commAmt);

        // Créditer le wallet du vendeur
        MarketplaceSeller seller = product.getSeller();
        seller.setWalletBalance(seller.getWalletBalance().add(sellerAmt));
        sellerRepo.save(seller);

        // Incrémenter les downloads
        product.setDownloads(product.getDownloads() + 1);
        productRepo.save(product);

        UserProfile buyer = userProfileRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        MarketplaceOrder order = MarketplaceOrder.builder()
                .product(product)
                .buyerUser(buyer)
                .amountPaid(price)
                .currency(product.getCurrency())
                .commissionRate(commRate)
                .commissionAmt(commAmt)
                .sellerAmount(sellerAmt)
                .paymentMethod(paymentMethod)
                .status(OrderStatus.COMPLETED)
                .downloadUrl(product.getFileUrl())
                .build();

        orderRepo.save(order);
        log.info("[MARKETPLACE] Achat | produit={} acheteur={} montant={}", productId, userId, price);
        return toOrderResponse(order);
    }

    @Override
    public Page<OrderResponse> myPurchases(Pageable pageable) {
        return orderRepo.findByBuyerSchoolId(SchoolContext.getSchoolId(), pageable)
                .map(this::toOrderResponse);
    }

    @Override
    public Page<OrderResponse> mySales(Pageable pageable) {
        UUID userId   = SchoolContext.getUserId();
        MarketplaceSeller seller = sellerRepo.findByUserProfileId(userId)
                .orElseThrow(() -> new RuntimeException("Profil vendeur introuvable"));
        return orderRepo.findByProductSellerId(seller.getId(), pageable)
                .map(this::toOrderResponse);
    }

    // ════════════════════════════════
    // WITHDRAWALS
    // ════════════════════════════════

    @Override
    @Transactional
    public WithdrawalResponse requestWithdrawal(WithdrawalRequest req) {
        UUID userId   = SchoolContext.getUserId();
        MarketplaceSeller seller = sellerRepo.findByUserProfileId(userId)
                .orElseThrow(() -> new RuntimeException("Profil vendeur introuvable"));

        if (withdrawalRepo.existsBySellerIdAndStatus(seller.getId(), WithdrawalStatus.PENDING)) {
            throw new RuntimeException("Vous avez déjà un retrait en attente.");
        }
        if (req.getAmount().compareTo(seller.getWalletBalance()) > 0) {
            throw new RuntimeException("Solde insuffisant.");
        }

        // Débiter le wallet
        seller.setWalletBalance(seller.getWalletBalance().subtract(req.getAmount()));
        sellerRepo.save(seller);

        MarketplaceWithdrawal withdrawal = MarketplaceWithdrawal.builder()
                .seller(seller)
                .amount(req.getAmount())
                .withdrawalMethod(req.getWithdrawalMethod())
                .mobileOperator(req.getMobileOperator())
                .mobileNumber(req.getMobileNumber())
                .bankName(req.getBankName())
                .bankAccountName(req.getBankAccountName())
                .bankAccountNumber(req.getBankAccountNumber())
                .bankIban(req.getBankIban())
                .bankSwift(req.getBankSwift())
                .bankCountry(req.getBankCountry())
                .status(WithdrawalStatus.PENDING)
                .build();

        withdrawalRepo.save(withdrawal);
        log.info("[MARKETPLACE] Retrait demandé | vendeur={} montant={} méthode={}",
                seller.getId(), req.getAmount(), req.getWithdrawalMethod());
        return toWithdrawalResponse(withdrawal);
    }

    @Override
    public Page<WithdrawalResponse> myWithdrawals(Pageable pageable) {
        UUID userId   = SchoolContext.getUserId();
        MarketplaceSeller seller = sellerRepo.findByUserProfileId(userId)
                .orElseThrow(() -> new RuntimeException("Profil vendeur introuvable"));
        return withdrawalRepo.findBySellerId(seller.getId(), pageable)
                .map(this::toWithdrawalResponse);
    }

    @Override
    public SellerDashboardResponse sellerDashboard() {
        UUID userId   = SchoolContext.getUserId();
        MarketplaceSeller seller = sellerRepo.findByUserProfileId(userId)
                .orElseThrow(() -> new RuntimeException("Profil vendeur introuvable"));

        BigDecimal totalEarned = orderRepo.totalEarnedBySeller(seller.getId());
        long totalProducts     = productRepo.findBySellerId(seller.getId(), Pageable.unpaged()).getTotalElements();
        long totalSales        = orderRepo.findByProductSellerId(seller.getId(), Pageable.unpaged()).getTotalElements();

        return SellerDashboardResponse.builder()
                .walletBalance(seller.getWalletBalance())
                .totalEarned(totalEarned)
                .totalProducts(totalProducts)
                .totalSales(totalSales)
                .build();
    }

    // ════════════════════════════════
    // MAPPERS
    // ════════════════════════════════

    private SellerResponse toSellerResponse(MarketplaceSeller s) {
        return SellerResponse.builder()
                .id(s.getId())
                .displayName(s.getDisplayName())
                .bio(s.getBio())
                .sellerType(s.getSellerType())
                .commissionRate(s.getCommissionRate())
                .walletBalance(s.getWalletBalance())
                .status(s.getStatus())
                .createdAt(s.getCreatedAt())
                .build();
    }

    private ProductResponse toProductResponse(MarketplaceProduct p) {
        double avg = p.getRatingCount() > 0
                ? p.getRatingSum().divide(new BigDecimal(p.getRatingCount()), 2, RoundingMode.HALF_UP).doubleValue()
                : 0.0;
        return ProductResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .description(p.getDescription())
                .category(p.getCategory())
                .subject(p.getSubject())
                .level(p.getLevel())
                .price(p.getPrice())
                .currency(p.getCurrency())
                .previewUrl(p.getPreviewUrl())
                .thumbnailUrl(p.getThumbnailUrl())
                .status(p.getStatus())
                .downloads(p.getDownloads())
                .averageRating(avg)
                .sellerName(p.getSeller().getDisplayName())
                .createdAt(p.getCreatedAt())
                .build();
    }

    private OrderResponse toOrderResponse(MarketplaceOrder o) {
        return OrderResponse.builder()
                .id(o.getId())
                .productId(o.getProduct().getId())
                .productTitle(o.getProduct().getTitle())
                .amountPaid(o.getAmountPaid())
                .currency(o.getCurrency())
                .commissionAmt(o.getCommissionAmt())
                .sellerAmount(o.getSellerAmount())
                .paymentMethod(o.getPaymentMethod())
                .status(o.getStatus())
                .downloadUrl(o.getDownloadUrl())
                .purchasedAt(o.getPurchasedAt())
                .build();
    }

    private WithdrawalResponse toWithdrawalResponse(MarketplaceWithdrawal w) {
        return WithdrawalResponse.builder()
                .id(w.getId())
                .amount(w.getAmount())
                .withdrawalMethod(w.getWithdrawalMethod())
                .mobileOperator(w.getMobileOperator())
                .mobileNumber(w.getMobileNumber())
                .bankName(w.getBankName())
                .status(w.getStatus())
                .reference(w.getReference())
                .requestedAt(w.getRequestedAt())
                .processedAt(w.getProcessedAt())
                .build();
    }
}
