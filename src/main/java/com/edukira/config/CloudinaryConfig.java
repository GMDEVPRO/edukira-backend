package com.edukira.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Value("${edukira.cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${edukira.cloudinary.api-key:}")
    private String apiKey;

    @Value("${edukira.cloudinary.api-secret:}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        if (cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {
            // Sandbox — retorna instância sem credenciais reais
            return new Cloudinary(Map.of(
                    "cloud_name", "sandbox",
                    "api_key",    "sandbox",
                    "api_secret", "sandbox",
                    "secure",     true
            ));
        }
        return new Cloudinary(Map.of(
                "cloud_name", cloudName,
                "api_key",    apiKey,
                "api_secret", apiSecret,
                "secure",     true
        ));
    }
}
