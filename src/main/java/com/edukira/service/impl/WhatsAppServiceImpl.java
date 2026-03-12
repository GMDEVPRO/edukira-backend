package com.edukira.service.impl;

import com.edukira.service.WhatsAppService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

@Service
@Slf4j
public class WhatsAppServiceImpl implements WhatsAppService {

    private final String accountSid;
    private final String authToken;
    private final String fromNumber;
    private final RestTemplate restTemplate = new RestTemplate();

    public WhatsAppServiceImpl(
            @org.springframework.beans.factory.annotation.Value("${edukira.twilio.account-sid:}") String accountSid,
            @org.springframework.beans.factory.annotation.Value("${edukira.twilio.auth-token:}") String authToken,
            @org.springframework.beans.factory.annotation.Value("${edukira.twilio.whatsapp-from:+14155238886}") String fromNumber) {
        this.accountSid = accountSid;
        this.authToken  = authToken;
        this.fromNumber = fromNumber;
    }

    @Override
    public String send(String phone, String message) {
        if (accountSid == null || accountSid.isBlank()) {
            String mockId = "WA_SANDBOX_" + System.currentTimeMillis();
            log.info("[WHATSAPP-SANDBOX] Para={} msg={}", phone, message);
            return mockId;
        }

        try {
            String url = "https://api.twilio.com/2010-04-01/Accounts/"
                    + accountSid + "/Messages.json";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            String creds = Base64.getEncoder()
                    .encodeToString((accountSid + ":" + authToken).getBytes());
            headers.set("Authorization", "Basic " + creds);

            String body = "From=whatsapp:" + fromNumber
                    + "&To=whatsapp:" + phone
                    + "&Body=" + message;

            ResponseEntity<Map> resp = restTemplate.postForEntity(
                    url, new HttpEntity<>(body, headers), Map.class);

            String sid = (String) resp.getBody().get("sid");
            log.info("[WHATSAPP] Enviado para={} sid={}", phone, sid);
            return sid;

        } catch (Exception e) {
            log.error("[WHATSAPP] Erro: {}", e.getMessage());
            return null;
        }
    }
}