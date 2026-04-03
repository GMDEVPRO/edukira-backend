package com.edukira.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    /**
     * Upload de imagem (capa/thumbnail) para Cloudinary.
     * Retorna a URL pública optimizada.
     */
    String uploadImage(MultipartFile file, String folder);

    /**
     * Upload de ficheiro digital (PDF/vídeo) para Backblaze B2.
     * Retorna a URL pública permanente.
     */
    String uploadFile(MultipartFile file, String folder);

    /**
     * Gera URL temporária assinada para download seguro (válida 1h).
     * Garante que só quem comprou pode descarregar.
     */
    String generateDownloadUrl(String fileKey, int expiryMinutes);

    /**
     * Remove ficheiro do Backblaze B2.
     */
    void deleteFile(String fileKey);

    /**
     * Remove imagem do Cloudinary.
     */
    void deleteImage(String publicId);
}