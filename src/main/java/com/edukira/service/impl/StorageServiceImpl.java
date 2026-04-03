package com.edukira.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.edukira.exception.EdukiraException;
import com.edukira.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageServiceImpl implements StorageService {

    private final Cloudinary  cloudinary;
    private final S3Client    b2S3Client;
    private final S3Presigner b2S3Presigner;

    @Value("${edukira.cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${edukira.cloudinary.folder:edukira/marketplace}")
    private String cloudinaryFolder;

    @Value("${edukira.backblaze.bucket-name:edukira-marketplace}")
    private String bucketName;

    @Value("${edukira.backblaze.key-id:}")
    private String b2KeyId;

    @Value("${edukira.backblaze.public-url:}")
    private String b2PublicUrl;

    // Tipos permitidos
    private static final Set<String> ALLOWED_IMAGE_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp");

    private static final Set<String> ALLOWED_FILE_TYPES =
            Set.of("application/pdf", "video/mp4", "video/mpeg",
                    "application/zip", "application/epub+zip");

    private static final long MAX_IMAGE_SIZE = 5L  * 1024 * 1024;   //   5 MB
    private static final long MAX_FILE_SIZE  = 500L * 1024 * 1024;  // 500 MB

    // ════════════════════════════════════════════════════════
    // CLOUDINARY — Imagens
    // ════════════════════════════════════════════════════════

    @Override
    public String uploadImage(MultipartFile file, String folder) {
        validateFile(file, ALLOWED_IMAGE_TYPES, MAX_IMAGE_SIZE, "imagem");

        // Sandbox — sem credenciais reais
        if (cloudName.isBlank() || cloudName.equals("sandbox")) {
            String mockUrl = "https://res.cloudinary.com/sandbox/image/upload/v1/" +
                    folder + "/mock_" + UUID.randomUUID().toString().substring(0, 8) + ".jpg";
            log.info("[CLOUDINARY-SANDBOX] Upload simulado | url={}", mockUrl);
            return mockUrl;
        }

        try {
            String publicId = (folder != null ? folder : cloudinaryFolder)
                    + "/" + UUID.randomUUID();

            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id",      publicId,
                            "overwrite",      false,
                            "resource_type",  "image",
                            "quality",        "auto",
                            "fetch_format",   "auto",
                            "transformation", "c_limit,w_1200,h_1200"
                    )
            );

            String url = (String) result.get("secure_url");
            log.info("[CLOUDINARY] Upload OK | publicId={} url={}", publicId, url);
            return url;

        } catch (IOException e) {
            log.error("[CLOUDINARY] Erro no upload: {}", e.getMessage());
            throw EdukiraException.badRequest("Erro ao fazer upload da imagem: " + e.getMessage());
        }
    }

    @Override
    public void deleteImage(String publicId) {
        if (cloudName.isBlank() || cloudName.equals("sandbox")) {
            log.info("[CLOUDINARY-SANDBOX] Delete simulado | publicId={}", publicId);
            return;
        }
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("[CLOUDINARY] Imagem removida | publicId={}", publicId);
        } catch (IOException e) {
            log.error("[CLOUDINARY] Erro ao remover imagem: {}", e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════
    // BACKBLAZE B2 — Ficheiros digitais
    // ════════════════════════════════════════════════════════

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        validateFile(file, ALLOWED_FILE_TYPES, MAX_FILE_SIZE, "ficheiro");

        String ext     = getExtension(file.getOriginalFilename());
        String fileKey = (folder != null ? folder : "products") + "/" +
                UUID.randomUUID() + "." + ext;

        // Sandbox — sem credenciais reais
        if (b2KeyId.isBlank() || b2KeyId.equals("sandbox")) {
            String mockUrl = "https://sandbox.backblaze.com/file/" + bucketName + "/" + fileKey;
            log.info("[B2-SANDBOX] Upload simulado | key={}", fileKey);
            return mockUrl;
        }

        try {
            PutObjectRequest req = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            b2S3Client.putObject(req, RequestBody.fromInputStream(
                    file.getInputStream(), file.getSize()));

            // URL pública (só se o bucket for público)
            String url = b2PublicUrl.isBlank()
                    ? "b2://" + bucketName + "/" + fileKey
                    : b2PublicUrl + "/" + fileKey;

            log.info("[B2] Upload OK | key={} size={}MB",
                    fileKey, file.getSize() / (1024 * 1024));
            return url;

        } catch (Exception e) {
            log.error("[B2] Erro no upload: {}", e.getMessage());
            throw EdukiraException.badRequest("Erro ao fazer upload do ficheiro: " + e.getMessage());
        }
    }

    @Override
    public String generateDownloadUrl(String fileKey, int expiryMinutes) {
        // Sandbox
        if (b2KeyId.isBlank() || b2KeyId.equals("sandbox")) {
            log.info("[B2-SANDBOX] URL temporária simulada | key={}", fileKey);
            return "https://sandbox.backblaze.com/download/" + fileKey + "?token=mock";
        }

        try {
            GetObjectPresignRequest presignReq = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expiryMinutes))
                    .getObjectRequest(r -> r.bucket(bucketName).key(fileKey))
                    .build();

            String url = b2S3Presigner.presignGetObject(presignReq)
                    .url().toString();

            log.info("[B2] URL temporária gerada | key={} expiry={}min", fileKey, expiryMinutes);
            return url;

        } catch (Exception e) {
            log.error("[B2] Erro ao gerar URL: {}", e.getMessage());
            throw EdukiraException.badRequest("Erro ao gerar link de download.");
        }
    }

    @Override
    public void deleteFile(String fileKey) {
        if (b2KeyId.isBlank() || b2KeyId.equals("sandbox")) {
            log.info("[B2-SANDBOX] Delete simulado | key={}", fileKey);
            return;
        }
        try {
            b2S3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName).key(fileKey).build());
            log.info("[B2] Ficheiro removido | key={}", fileKey);
        } catch (Exception e) {
            log.error("[B2] Erro ao remover ficheiro: {}", e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════
    // Helpers
    // ════════════════════════════════════════════════════════

    private void validateFile(MultipartFile file, Set<String> allowedTypes,
                              long maxSize, String label) {
        if (file == null || file.isEmpty()) {
            throw EdukiraException.badRequest("Ficheiro " + label + " não pode estar vazio.");
        }
        if (!allowedTypes.contains(file.getContentType())) {
            throw EdukiraException.badRequest(
                    "Tipo de " + label + " não permitido: " + file.getContentType() +
                            ". Permitidos: " + allowedTypes);
        }
        if (file.getSize() > maxSize) {
            throw EdukiraException.badRequest(
                    "Tamanho do " + label + " excede o limite de " +
                            (maxSize / (1024 * 1024)) + "MB.");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "bin";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
