package com.edukira.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class BackblazeConfig {

    @Value("${edukira.backblaze.endpoint:https://s3.us-west-004.backblazeb2.com}")
    private String endpoint;

    @Value("${edukira.backblaze.key-id:}")
    private String keyId;

    @Value("${edukira.backblaze.app-key:}")
    private String appKey;

    @Bean
    public S3Client b2S3Client() {
        String resolvedKeyId = keyId.isBlank() ? "sandbox" : keyId;
        String resolvedAppKey = appKey.isBlank() ? "sandbox" : appKey;

        return S3Client.builder()

                .region(Region.US_EAST_1)
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(resolvedKeyId, resolvedAppKey)))
                .build();
    }

    @Bean
    public S3Presigner b2S3Presigner() {
        String resolvedKeyId = keyId.isBlank() ? "sandbox" : keyId;
        String resolvedAppKey = appKey.isBlank() ? "sandbox" : appKey;

        return S3Presigner.builder()

                .region(Region.US_EAST_1)
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(resolvedKeyId, resolvedAppKey)))
                .build();
    }
}