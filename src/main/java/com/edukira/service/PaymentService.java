package com.edukira.service;
import com.edukira.dto.request.PaymentInitRequest;
import com.edukira.dto.response.PaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PaymentService {
    Page<PaymentResponse> findAll(UUID schoolId, Pageable pageable);
    List<PaymentResponse> findOverdue(UUID schoolId);
    Map<String, Object> initMobileMoney(PaymentInitRequest request, UUID schoolId);
    void processWebhook(String provider, Map<String, Object> payload);
}
