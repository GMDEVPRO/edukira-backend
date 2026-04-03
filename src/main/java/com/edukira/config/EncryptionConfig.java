package com.edukira.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class EncryptionConfig {

    @Value("${edukira.encryption.secret-key}")
    private String secretKeyBase64;

    @Bean
    public SecretKey aesSecretKey() {
        byte[] decoded = Base64.getDecoder().decode(secretKeyBase64);
        if (decoded.length != 32) {
            throw new IllegalStateException(
                    "ENCRYPTION_KEY deve ser uma chave AES-256 de 32 bytes (Base64). " +
                            "Gere com: openssl rand -base64 32");
        }
        return new SecretKeySpec(decoded, "AES");
    }
}
