package com.edukira.service.impl;


import com.edukira.dto.request.PaymentInitRequest;
import com.edukira.dto.response.PaymentResponse;
import com.edukira.entity.Payment;
import com.edukira.entity.School;
import com.edukira.entity.Student;
import com.edukira.enums.PaymentStatus;
import com.edukira.exception.EdukiraException;
import com.edukira.repository.PaymentRepository;
import com.edukira.repository.SchoolRepository;
import com.edukira.repository.StudentRepository;
import com.edukira.service.PaymentService;
import com.edukira.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository  paymentRepo;
    private final StudentRepository  studentRepo;
    private final SchoolRepository   schoolRepo;
    private final NotificationService notificationService;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${edukira.mtn-momo.api-user-id:}")
    private String globalMtnApiUserId;

    @Value("${edukira.mtn-momo.api-user-key:}")
    private String globalMtnApiUserKey;

    @Value("${edukira.wave.api-key:}")
    private String globalWaveApiKey;

    @Value("${edukira.orange-money.client-id:}")
    private String globalOrangeClientId;

    @Value("${edukira.orange-money.client-secret:}")
    private String globalOrangeClientSecret;

    @Value("${edukira.mtn-momo.subscription-key:}")
    private String globalMtnSubscriptionKey;

    @Value("${edukira.mtn-momo.environment:sandbox}")
    private String mtnEnvironment;

    private static final String WAVE_BASE     = "https://api.wave.com/v1";
    private static final String ORANGE_TOKEN  = "https://api.orange.com/oauth/v3/token";
    private static final String ORANGE_BASE   = "https://api.orange.com/orange-money-webpay/ci/v1";
    private static final String MTN_BASE_PROD = "https://proxy.momoapi.mtn.com";
    private static final String MTN_BASE_SBX  = "https://sandbox.momodeveloper.mtn.com";

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> findAll(UUID schoolId, Pageable pageable) {
        return paymentRepo.findBySchoolId(schoolId, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> findOverdue(UUID schoolId) {
        return paymentRepo.findBySchoolIdAndStatus(schoolId, PaymentStatus.OVERDUE)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public Map<String, Object> initMobileMoney(PaymentInitRequest req, UUID schoolId) {

        School  school  = requireSchool(schoolId);
        Student student = requireStudent(req.getStudentId(), schoolId);

        String month = req.getMonth() != null ? req.getMonth()
                : YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        paymentRepo.findBySchoolIdAndStatus(schoolId, PaymentStatus.PENDING).stream()
                .filter(p -> p.getStudent().getId().equals(req.getStudentId())
                        && p.getMonth().equals(month))
                .findFirst()
                .ifPresent(p -> {
                    throw EdukiraException.badRequest(
                            "Já existe um pagamento pendente para " + month +
                                    ". SessionId: " + p.getSessionId());
                });

        Payment payment = Payment.builder()
                .student(student)
                .school(school)
                .amount(req.getAmount())
                .currency(req.getCurrency() != null ? req.getCurrency() : "XOF")
                .month(month)
                .method(req.getMethod())
                .status(PaymentStatus.PENDING)
                .dueDate(Instant.now().plusSeconds(3600))
                .build();

        Map<String, Object> providerResult = switch (req.getMethod()) {
            case WAVE         -> initWave(req, school, payment);
            case ORANGE_MONEY -> initOrangeMoney(req, school, payment);
            case MTN_MOMO     -> initMtnMomo(req, school, payment);
            case CASH         -> initCash(req, school, payment);
            default           -> throw EdukiraException.badRequest(
                    "Método não suportado: " + req.getMethod());
        };

        paymentRepo.save(payment);
        log.info("[PAYMENT] Iniciado | aluno={} mês={} método={} valor={}",
                student.getId(), month, req.getMethod(), req.getAmount());

        return providerResult;
    }

    @Override
    @Transactional
    public void processWebhook(String provider, Map<String, Object> payload) {
        log.info("[WEBHOOK] Provider={} keys={}", provider, payload.keySet());
        switch (provider) {
            case "WAVE"         -> processWaveWebhook(payload);
            case "ORANGE_MONEY" -> processOrangeWebhook(payload);
            case "MTN_MOMO"     -> processMtnWebhook(payload);
            default             -> log.warn("[WEBHOOK] Provider desconhecido: {}", provider);
        }
    }

    private Map<String, Object> initWave(PaymentInitRequest req, School school, Payment payment) {
        String apiKey = nonEmpty(school.getWaveApiKey(), globalWaveApiKey);

        if (apiKey.isEmpty()) {
            String mockId  = "cos_SANDBOX_" + uuid8();
            String mockUrl = "https://pay.wave.com/m/sandbox?session=" + mockId;
            payment.setSessionId(mockId);
            log.info("[WAVE-SANDBOX] checkout_url={}", mockUrl);
            return Map.of(
                    "provider", "WAVE",
                    "session_id", mockId,
                    "checkout_url", mockUrl,
                    "sandbox", true,
                    "expires_at", Instant.now().plusSeconds(3600).toString()
            );
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("amount", req.getAmount().longValue());
            body.put("currency", req.getCurrency() != null ? req.getCurrency() : "XOF");
            body.put("client_reference", payment.getMonth() + "_" + req.getStudentId());
            body.put("success_url", "https://app.edukira.com/payment/success");
            body.put("error_url",   "https://app.edukira.com/payment/error");
            body.put("restricted_payment_method", "wave-senegal");

            ResponseEntity<Map> resp = restTemplate.postForEntity(
                    WAVE_BASE + "/checkout/sessions",
                    new HttpEntity<>(body, headers), Map.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = resp.getBody();
            String sessionId = (String) data.get("id");
            String url       = (String) data.get("wave_launch_url");

            payment.setSessionId(sessionId);
            log.info("[WAVE] Sessão criada: {}", sessionId);

            return Map.of(
                    "provider", "WAVE",
                    "session_id", sessionId,
                    "checkout_url", url,
                    "expires_at", data.getOrDefault("expires_at", "")
            );

        } catch (Exception e) {
            log.error("[WAVE] Erro: {}", e.getMessage());
            throw EdukiraException.badRequest("Erro ao contactar Wave: " + e.getMessage());
        }
    }

    private void processWaveWebhook(Map<String, Object> payload) {
        String type = (String) payload.get("type");
        if (!"checkout.session.completed".equals(type)) {
            log.info("[WAVE-WEBHOOK] Evento ignorado: {}", type);
            return;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) payload.getOrDefault("data", Map.of());
        String sessionId = (String) data.get("id");
        if (sessionId == null) { log.warn("[WAVE-WEBHOOK] sem session_id"); return; }

        paymentRepo.findBySessionId(sessionId).ifPresentOrElse(p -> {
            p.setStatus(PaymentStatus.PAID);
            p.setPaidAt(Instant.now());
            p.setTransactionId((String) data.getOrDefault("transaction_id", sessionId));
            paymentRepo.save(p);
            log.info("[WAVE-WEBHOOK] Confirmado: session={}", sessionId);
            // ── Notificação ao responsável ────────────────────────────
            if (p.getStudent() != null) {
                Student s = p.getStudent();
                notificationService.notifyPaymentConfirmed(
                        p.getSchool().getId(), s.getId(),
                        s.getFirstName() + " " + s.getLastName(),
                        s.getGuardianPhone(), s.getGuardianName(),
                        p.getAmount().toPlainString(), p.getCurrency(), p.getMonth());
            }
        }, () -> log.warn("[WAVE-WEBHOOK] Não encontrado: session={}", sessionId));
    }

    private Map<String, Object> initOrangeMoney(PaymentInitRequest req, School school, Payment payment) {
        String clientId     = nonEmpty(school.getOrangeApiKey(), globalOrangeClientId);
        String clientSecret = globalOrangeClientSecret;

        if (clientId.isEmpty()) {
            String mockToken = "OMP_SANDBOX_" + uuid8();
            String mockUrl   = "https://api.orange.com/sandbox/webpay?token=" + mockToken;
            payment.setSessionId(mockToken);
            log.info("[ORANGE-SANDBOX] pay_url={}", mockUrl);
            return Map.of(
                    "provider", "ORANGE_MONEY",
                    "pay_token", mockToken,
                    "payment_url", mockUrl,
                    "sandbox", true
            );
        }

        try {
            String accessToken = getOrangeAccessToken(clientId, clientSecret);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            String orderId = payment.getMonth() + "_" + uuid8();

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("merchant_key", clientId);
            body.put("currency", "OUV");
            body.put("order_id", orderId);
            body.put("amount", req.getAmount().intValue());
            body.put("return_url", "https://app.edukira.com/payment/success");
            body.put("cancel_url", "https://app.edukira.com/payment/cancel");
            body.put("notif_url",  "https://api.edukira.com/v1/payments/webhook");
            body.put("lang", "fr");

            ResponseEntity<Map> resp = restTemplate.postForEntity(
                    ORANGE_BASE + "/webpayment",
                    new HttpEntity<>(body, headers), Map.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = resp.getBody();
            String payToken = (String) data.get("pay_token");
            String payUrl   = (String) data.get("payment_url");

            payment.setSessionId(payToken);
            log.info("[ORANGE] pay_token={}", payToken);

            return Map.of(
                    "provider", "ORANGE_MONEY",
                    "pay_token", payToken,
                    "payment_url", payUrl,
                    "order_id", orderId
            );

        } catch (Exception e) {
            log.error("[ORANGE] Erro: {}", e.getMessage());
            throw EdukiraException.badRequest("Erro ao contactar Orange Money: " + e.getMessage());
        }
    }

    private String getOrangeAccessToken(String clientId, String clientSecret) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String creds = Base64.getEncoder().encodeToString(
                (clientId + ":" + clientSecret).getBytes());
        h.set("Authorization", "Basic " + creds);

        ResponseEntity<Map> resp = restTemplate.postForEntity(
                ORANGE_TOKEN,
                new HttpEntity<>("grant_type=client_credentials", h),
                Map.class);

        return (String) resp.getBody().get("access_token");
    }

    private void processOrangeWebhook(Map<String, Object> payload) {
        String status   = (String) payload.getOrDefault("status", "");
        String payToken = (String) payload.get("pay_token");
        String txnId    = (String) payload.get("txnid");

        if (payToken == null) { log.warn("[ORANGE-WEBHOOK] sem pay_token"); return; }

        paymentRepo.findBySessionId(payToken).ifPresentOrElse(p -> {
            if ("SUCCESS".equalsIgnoreCase(status)) {
                p.setStatus(PaymentStatus.PAID);
                p.setPaidAt(Instant.now());
                p.setTransactionId(txnId);
                paymentRepo.save(p);
                log.info("[ORANGE-WEBHOOK] Pago: pay_token={}", payToken);
            } else {
                log.warn("[ORANGE-WEBHOOK] Status: {}", status);
            }
        }, () -> log.warn("[ORANGE-WEBHOOK] Não encontrado: pay_token={}", payToken));
    }

    // ── dentro de PaymentServiceImpl ──────────────────────────────────

    private Map<String, Object> initMtnMomo(PaymentInitRequest req, School school, Payment payment) {
        String subscriptionKey = nonEmpty(school.getMtnSubscriptionKey(), globalMtnSubscriptionKey);

        // ── Sandbox automático se sem credenciais ────────────────────
        if (subscriptionKey.isEmpty()) {
            String mockRef = "MTN_SANDBOX_" + UUID.randomUUID();
            payment.setSessionId(mockRef);
            log.info("[MTN-SANDBOX] referenceId={}", mockRef);
            return Map.of(
                    "provider",      "MTN_MOMO",
                    "reference_id",  mockRef,
                    "status",        "PENDING",
                    "sandbox",       true,
                    "message",       "Pedido enviado ao telemóvel do cliente"
            );
        }

        try {
            String baseUrl     = "sandbox".equals(mtnEnvironment) ? MTN_BASE_SBX : MTN_BASE_PROD;
            String referenceId = UUID.randomUUID().toString();
            String targetPhone = req.getPhone() != null
                    ? req.getPhone().replaceAll("[^0-9]", "") : "";

            // ── PASSO 1: Obter Bearer token OAuth2 ──────────────────
            String bearerToken = getMtnBearerToken(baseUrl, subscriptionKey);

            // ── PASSO 2: RequestToPay ────────────────────────────────
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(bearerToken);
            headers.set("X-Reference-Id",       referenceId);
            headers.set("X-Target-Environment", mtnEnvironment);
            headers.set("Ocp-Apim-Subscription-Key", subscriptionKey);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("amount",     req.getAmount().toPlainString());
            body.put("currency",   req.getCurrency() != null ? req.getCurrency() : "XOF");
            body.put("externalId", payment.getMonth() + "_" + req.getStudentId());
            body.put("payer",      Map.of("partyIdType", "MSISDN", "partyId", targetPhone));
            body.put("payerMessage", "Mensalidade Edukira " + payment.getMonth());
            body.put("payeeNote",    "Escola " + school.getName());

            restTemplate.postForEntity(
                    baseUrl + "/collection/v1_0/requesttopay",
                    new HttpEntity<>(body, headers),
                    Void.class);

            payment.setSessionId(referenceId);
            log.info("[MTN] RequestToPay enviado | referenceId={}", referenceId);

            return Map.of(
                    "provider",     "MTN_MOMO",
                    "reference_id", referenceId,
                    "status",       "PENDING",
                    "message",      "Pedido enviado. Aguarda confirmação no telemóvel."
            );

        } catch (Exception e) {
            log.error("[MTN] Erro: {}", e.getMessage());
            throw EdukiraException.badRequest("Erro ao contactar MTN MoMo: " + e.getMessage());
        }
    }

    /**
     * PASSO 1 — Obtém o Bearer token OAuth2 da MTN MoMo.
     *
     * POST {baseUrl}/collection/token/
     * Authorization: Basic Base64(apiUserId:apiUserKey)
     * Ocp-Apim-Subscription-Key: {subscriptionKey}
     *
     * Retorna: { "access_token": "...", "token_type": "access_token", "expires_in": 3600 }
     */
    private String getMtnBearerToken(String baseUrl, String subscriptionKey) {
        // Em produção o apiUserId e apiUserKey são gerados via /v1_0/apiuser
        // e guardados nas variáveis de ambiente MTN_API_USER_ID e MTN_API_USER_KEY
        String apiUserId  = nonEmpty(globalMtnApiUserId,  "sandbox_user");
        String apiUserKey = nonEmpty(globalMtnApiUserKey, "sandbox_key");

        String credentials = Base64.getEncoder().encodeToString(
                (apiUserId + ":" + apiUserKey).getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization",             "Basic " + credentials);
        headers.set("Ocp-Apim-Subscription-Key", subscriptionKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<Map> resp = restTemplate.postForEntity(
                    baseUrl + "/collection/token/",
                    new HttpEntity<>(headers),
                    Map.class);

            String token = (String) resp.getBody().get("access_token");
            if (token == null || token.isBlank()) {
                throw new RuntimeException("MTN não retornou access_token");
            }
            log.info("[MTN] Bearer token obtido com sucesso");
            return token;

        } catch (Exception e) {
            log.error("[MTN] Falha ao obter Bearer token: {}", e.getMessage());
            throw EdukiraException.badRequest("MTN MoMo auth falhou: " + e.getMessage());
        }
    }
    private void processMtnWebhook(Map<String, Object> payload) {
        String referenceId = (String) payload.get("referenceId");
        String status      = (String) payload.getOrDefault("status", "");
        String txnId       = (String) payload.get("financialTransactionId");

        if (referenceId == null) { log.warn("[MTN-WEBHOOK] sem referenceId"); return; }

        paymentRepo.findBySessionId(referenceId).ifPresentOrElse(p -> {
            if ("SUCCESSFUL".equalsIgnoreCase(status)) {
                p.setStatus(PaymentStatus.PAID);
                p.setPaidAt(Instant.now());
                p.setTransactionId(txnId);
                paymentRepo.save(p);
                log.info("[MTN-WEBHOOK] Pago: referenceId={}", referenceId);
            } else if ("FAILED".equalsIgnoreCase(status)) {
                log.warn("[MTN-WEBHOOK] Falhou: referenceId={}", referenceId);
            }
        }, () -> log.warn("[MTN-WEBHOOK] Não encontrado: referenceId={}", referenceId));
    }

    private Map<String, Object> initCash(PaymentInitRequest req, School school, Payment payment) {
        String receiptId = "CASH_" + uuid8();
        payment.setSessionId(receiptId);
        payment.setTransactionId(receiptId);
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(Instant.now());
        log.info("[CASH] Pagamento presencial: {}", receiptId);
        return Map.of(
                "provider", "CASH",
                "receipt_id", receiptId,
                "status", "PAID",
                "paid_at", Instant.now().toString()
        );
    }

    private School requireSchool(UUID schoolId) {
        return schoolRepo.findById(schoolId)
                .orElseThrow(() -> EdukiraException.notFound("Escola"));
    }

    private Student requireStudent(UUID studentId, UUID schoolId) {
        return studentRepo.findByIdAndSchoolId(studentId, schoolId)
                .orElseThrow(() -> EdukiraException.notFound("Aluno"));
    }

    private String nonEmpty(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return "";
    }

    private String uuid8() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private PaymentResponse toResponse(Payment p) {
        String studentName = p.getStudent() != null
                ? p.getStudent().getFirstName() + " " + p.getStudent().getLastName()
                : "";
        return PaymentResponse.builder()
                .id(p.getId().toString())
                .studentId(p.getStudent() != null ? p.getStudent().getId().toString() : null)
                .studentName(studentName)
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .month(p.getMonth())
                .status(p.getStatus().name())
                .method(p.getMethod().name())
                .receiptUrl(p.getReceiptUrl())
                .paidAt(p.getPaidAt())
                .build();
    }
}