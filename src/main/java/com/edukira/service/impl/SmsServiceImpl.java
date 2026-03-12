package com.edukira.service.impl;

import com.edukira.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
public class SmsServiceImpl implements SmsService {

    private final String apiKey;
    private final String username;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String AT_URL     = "https://api.africastalking.com/version1/messaging";
    private static final String AT_SANDBOX = "https://api.sandbox.africastalking.com/version1/messaging";

    public SmsServiceImpl(
            @org.springframework.beans.factory.annotation.Value("${edukira.africastalking.api-key:}") String apiKey,
            @org.springframework.beans.factory.annotation.Value("${edukira.africastalking.username:sandbox}") String username) {
        this.apiKey   = apiKey;
        this.username = username;
    }

    @Override
    public String send(String phone, String message) {
        if (apiKey == null || apiKey.isBlank()) {
            String mockId = "SMS_SANDBOX_" + System.currentTimeMillis();
            log.info("[SMS-SANDBOX] Para={} msg={}", phone, message);
            return mockId;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("apiKey", apiKey);
            headers.set("Accept", "application/json");

            String body = "username=" + username
                    + "&to=" + phone
                    + "&message=" + message;

            String url = "sandbox".equals(username) ? AT_SANDBOX : AT_URL;

            ResponseEntity<Map> resp = restTemplate.postForEntity(
                    url, new HttpEntity<>(body, headers), Map.class);

            log.info("[SMS] Enviado para={} status={}", phone, resp.getStatusCode());
            return "AT_" + System.currentTimeMillis();

        } catch (Exception e) {
            log.error("[SMS] Erro: {}", e.getMessage());
            return null;
        }
    }
}
