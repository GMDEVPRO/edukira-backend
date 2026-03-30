package com.edukira.service;

import com.edukira.dto.request.LeadRequest;
import com.edukira.dto.response.LeadResponse;
import com.edukira.enums.LeadStatus;

import java.util.List;
import java.util.UUID;

public interface LeadService {
    LeadResponse create(LeadRequest request);
    List<LeadResponse> findAll();
    List<LeadResponse> findByStatus(LeadStatus status);
    LeadResponse updateStatus(UUID id, LeadStatus status);
}
