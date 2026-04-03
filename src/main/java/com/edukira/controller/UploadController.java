package com.edukira.controller;

import com.edukira.dto.response.ApiResponse;
import com.edukira.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/v1/upload")
@RequiredArgsConstructor
@Tag(name = "Upload", description = "Upload de imagens e ficheiros para o Marketplace")
public class UploadController {

    private final StorageService storageService;

    @PostMapping(value = "/thumbnail",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Upload da capa/thumbnail do produto (JPG, PNG, WEBP · máx. 5MB)")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadThumbnail(
            @RequestPart("file") MultipartFile file) {
        String url = storageService.uploadImage(file, "marketplace/thumbnails");
        return ResponseEntity.ok(ApiResponse.ok(Map.of("url", url)));
    }

    @PostMapping(value = "/preview",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Upload da prévia/amostra do produto (JPG, PNG · máx. 5MB)")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadPreview(
            @RequestPart("file") MultipartFile file) {
        String url = storageService.uploadImage(file, "marketplace/previews");
        return ResponseEntity.ok(ApiResponse.ok(Map.of("url", url)));
    }

    @PostMapping(value = "/file",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Upload do ficheiro digital (PDF, MP4, EPUB, ZIP · máx. 500MB)")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadFile(
            @RequestPart("file") MultipartFile file) {
        String url = storageService.uploadFile(file, "marketplace/products");
        return ResponseEntity.ok(ApiResponse.ok(Map.of("url", url)));
    }

    @GetMapping("/download-url")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Gerar URL temporária de download seguro (válida 60 min)")
    public ResponseEntity<ApiResponse<Map<String, String>>> downloadUrl(
            @RequestParam String fileKey) {
        String url = storageService.generateDownloadUrl(fileKey, 60);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("downloadUrl", url)));
    }
}
