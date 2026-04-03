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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Pagamentos & Mobile Money")
public class PaymentController {

    private final PaymentService paymentService;

    @Value("${edukira.wave.webhook-secret:}")
    private String waveWebhookSecret;

    @Value("${edukira.orange-money.webhook-secret:}")
    private String orangeWebhookSecret;

    @Value("${edukira.mtn-momo.webhook-secret:}")
    private String mtnWebhookSecret;

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
            @RequestBody String rawBody) {

        String provider = detectProvider(headers, rawBody);

        // ── Validação de assinatura HMAC ─────────────────────────────
        if (!validateSignature(provider, headers, rawBody)) {
            log.warn("[WEBHOOK] Assinatura inválida | provider={}", provider);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Parse manual do body após validação
        Map<String, Object> payload = parseJson(rawBody);
        paymentService.processWebhook(provider, payload);
        return ResponseEntity.ok().build();
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private String detectProvider(Map<String, String> headers, String body) {
        if (headers.containsKey("x-wave-signature")) return "WAVE";
        if (body.contains("pay_token") || body.contains("order_id")) return "ORANGE_MONEY";
        if (body.contains("referenceId") || body.contains("financialTransactionId")) return "MTN_MOMO";
        return "UNKNOWN";
    }

    private boolean validateSignature(String provider, Map<String, String> headers, String body) {
        try {
            return switch (provider) {
                case "WAVE" -> {
                    if (waveWebhookSecret.isBlank()) { log.warn("[WEBHOOK] WAVE secret não configurado — modo sandbox"); yield true; }
                    String sig = headers.getOrDefault("x-wave-signature", "");
                    yield sig.equals(hmacSha256(waveWebhookSecret, body));
                }
                case "ORANGE_MONEY" -> {
                    if (orangeWebhookSecret.isBlank()) { log.warn("[WEBHOOK] ORANGE secret não configurado — modo sandbox"); yield true; }
                    String sig = headers.getOrDefault("x-orange-signature", "");
                    yield sig.equals(hmacSha256(orangeWebhookSecret, body));
                }
                case "MTN_MOMO" -> {
                    if (mtnWebhookSecret.isBlank()) { log.warn("[WEBHOOK] MTN secret não configurado — modo sandbox"); yield true; }
                    String sig = headers.getOrDefault("x-callback-token", "");
                    yield sig.equals(mtnWebhookSecret); // MTN usa token fixo
                }
                default -> {
                    log.warn("[WEBHOOK] Provider desconhecido — rejeitado");
                    yield false;
                }
            };
        } catch (Exception e) {
            log.error("[WEBHOOK] Erro na validação de assinatura: {}", e.getMessage());
            return false;
        }
    }

    private String hmacSha256(String secret, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String raw) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper =
                    new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(raw, Map.class);
        } catch (Exception e) {
            return Map.of();
        }
    }
}