package com.edukira.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Conversor JPA — criptografa/descriptografa colunas sensíveis com AES-256-GCM.
 *
 * Uso na entidade:
 *   @Convert(converter = AesEncryptor.class)
 *   private String waveApiKey;
 */
@Converter
@Component
@Slf4j
public class AesEncryptor implements AttributeConverter<String, String> {

    private static final String ALGORITHM  = "AES/GCM/NoPadding";
    private static final int    GCM_IV_LEN = 12;   // bytes
    private static final int    GCM_TAG    = 128;  // bits

    // Injectado pelo Spring; necessário ser static para o JPA poder usar
    private static SecretKey SECRET_KEY;

    @Autowired
    public void setSecretKey(SecretKey secretKey) {
        AesEncryptor.SECRET_KEY = secretKey;
    }

    @Override
    public String convertToDatabaseColumn(String plainText) {
        if (plainText == null || plainText.isBlank()) return null;
        if (SECRET_KEY == null) {
            log.warn("[CRYPTO] SecretKey não inicializada — guardando em texto plano");
            return plainText;
        }
        try {
            byte[] iv = new byte[GCM_IV_LEN];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, SECRET_KEY, new GCMParameterSpec(GCM_TAG, iv));

            byte[] encrypted = cipher.doFinal(plainText.getBytes());

            // Formato guardado no banco: Base64(iv + ciphertext)
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            log.error("[CRYPTO] Erro ao encriptar: {}", e.getMessage());
            throw new RuntimeException("Falha na encriptação AES-256", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String cipherBase64) {
        if (cipherBase64 == null || cipherBase64.isBlank()) return null;
        if (SECRET_KEY == null) {
            log.warn("[CRYPTO] SecretKey não inicializada — retornando valor bruto");
            return cipherBase64;
        }
        try {
            byte[] combined = Base64.getDecoder().decode(cipherBase64);

            byte[] iv         = new byte[GCM_IV_LEN];
            byte[] cipherText = new byte[combined.length - GCM_IV_LEN];
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LEN);
            System.arraycopy(combined, GCM_IV_LEN, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, SECRET_KEY, new GCMParameterSpec(GCM_TAG, iv));

            return new String(cipher.doFinal(cipherText));

        } catch (Exception e) {
            log.error("[CRYPTO] Erro ao desencriptar: {}", e.getMessage());
            // Retorna null em vez de explodir — evita quebrar o sistema
            // se algum valor antigo ainda estiver em texto plano
            return null;
        }
    }
}
