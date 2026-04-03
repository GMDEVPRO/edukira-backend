package com.edukira.controller;

import com.edukira.entity.Payment;
import com.edukira.exception.EdukiraException;
import com.edukira.repository.PaymentRepository;
import com.edukira.security.SchoolContext;
import com.edukira.service.PdfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/pdf")
@RequiredArgsConstructor
@Tag(name = "PDF", description = "Geração de boletins e recibos em PDF")
public class PdfController {

    private final PdfService        pdfService;
    private final PaymentRepository paymentRepo;

    @GetMapping("/report-card/{studentId}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','ADMIN','TEACHER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Gerar boletim PDF de um aluno")
    public ResponseEntity<byte[]> reportCard(
            @PathVariable UUID studentId,
            @RequestParam String period,   // BIMESTRE_1 | BIMESTRE_2 | BIMESTRE_3
            @RequestParam String year) {   // ex: 2025-2026

        UUID schoolId = SchoolContext.getSchoolId();
        byte[] pdf = pdfService.generateReportCard(studentId, schoolId, period, year);

        return ResponseEntity.ok()
                .headers(pdfHeaders("boletim_" + studentId + "_" + period + ".pdf"))
                .body(pdf);
    }

    @GetMapping("/receipt/{paymentId}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Gerar recibo PDF de um pagamento")
    public ResponseEntity<byte[]> receipt(@PathVariable UUID paymentId) {
        UUID schoolId = SchoolContext.getSchoolId();

        Payment payment = paymentRepo.findById(paymentId)
                .filter(p -> p.getSchool().getId().equals(schoolId))
                .orElseThrow(() -> EdukiraException.notFound("Pagamento"));

        byte[] pdf = pdfService.generatePaymentReceipt(payment);

        return ResponseEntity.ok()
                .headers(pdfHeaders("recibo_" + paymentId + ".pdf"))
                .body(pdf);
    }

    private HttpHeaders pdfHeaders(String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment().filename(filename).build());
        return headers;
    }
}
