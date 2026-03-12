package com.edukira.controller;

import com.edukira.dto.request.PaymentInitRequest;
import com.edukira.dto.response.ApiResponse;
import com.edukira.dto.response.PaymentResponse;
import com.edukira.service.PaymentService;
import com.edukira.util.TenantUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Pagamentos & Mobile Money")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Listar pagamentos da escola")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> findAll(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                paymentService.findAll(TenantUtil.currentSchoolId(), pageable)));
    }

    @GetMapping("/overdue")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Listar pagamentos em atraso")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> overdue() {
        return ResponseEntity.ok(ApiResponse.ok(
                paymentService.findOverdue(TenantUtil.currentSchoolId())));
    }

    @PostMapping("/mobile-money/init")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Iniciar pagamento Wave / Orange Money / MTN MoMo")
    public ResponseEntity<ApiResponse<Map<String, Object>>> initMobileMoney(
            @Valid @RequestBody PaymentInitRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                paymentService.initMobileMoney(request, TenantUtil.currentSchoolId())));
    }

    @PostMapping("/webhook")
    @Operation(summary = "Webhook unificado — Wave / Orange / MTN (público)")
    public ResponseEntity<Void> webhook(
            @RequestHeader Map<String, String> headers,
            @RequestBody Map<String, Object> payload) {
        // Detecta provider pelo header ou campo do body
        String provider = detectProvider(headers, payload);
        paymentService.processWebhook(provider, payload);
        return ResponseEntity.ok().build();
    }

    private String detectProvider(Map<String, String> headers, Map<String, Object> payload) {
        if (headers.containsKey("x-wave-signature")) return "WAVE";
        if (payload.containsKey("pay_token") || payload.containsKey("order_id")) return "ORANGE_MONEY";
        if (payload.containsKey("referenceId") || payload.containsKey("financialTransactionId")) return "MTN_MOMO";
        return "UNKNOWN";
    }
}
