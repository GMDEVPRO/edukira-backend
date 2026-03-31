package com.edukira.controller;

import com.edukira.dto.response.ApiResponse;
import com.edukira.dto.response.TransactionResponse;
import com.edukira.dto.response.WalletResponse;
import com.edukira.enums.TransactionType;
import com.edukira.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/wallet")
@RequiredArgsConstructor
@Tag(name = "Wallet", description = "Carteira digital do vendedor")
public class WalletController {

    private final WalletService walletService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Saldo e resumo da carteira do vendedor autenticado")
    public ResponseEntity<ApiResponse<WalletResponse>> getMyWallet() {
        return ResponseEntity.ok(ApiResponse.ok(walletService.getMyWallet()));
    }

    @GetMapping("/transactions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Histórico de transacções da carteira")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> myTransactions(
            @RequestParam(required = false) TransactionType type,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(walletService.myTransactions(type, pageable)));
    }
}
