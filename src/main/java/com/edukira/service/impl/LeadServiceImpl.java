package com.edukira.service.impl;

import com.edukira.dto.request.LeadRequest;
import com.edukira.dto.response.LeadResponse;
import com.edukira.entity.Lead;
import com.edukira.enums.LeadStatus;
import com.edukira.exception.EdukiraException;
import com.edukira.repository.LeadRepository;
import com.edukira.service.LeadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadServiceImpl implements LeadService {

    private final LeadRepository leadRepository;

    @Override
    @Transactional
    public LeadResponse create(LeadRequest request) {
        log.info("Novo lead recebido da landing: {} <{}>", request.getName(), request.getEmail());

        Lead lead = Lead.builder()
                .name(request.getName())
                .email(request.getEmail())
                .school(request.getSchool())
                .phone(request.getPhone())
                .message(request.getMessage())
                .language(request.getLanguage() != null ? request.getLanguage() : "fr")
                .build();

        Lead saved = leadRepository.save(lead);
        log.info("Lead salvo com id={}", saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeadResponse> findAll() {
        return leadRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeadResponse> findByStatus(LeadStatus status) {
        return leadRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LeadResponse updateStatus(UUID id, LeadStatus status) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new EdukiraException("Lead não encontrado", HttpStatus.NOT_FOUND));
        lead.setStatus(status);
        return toResponse(leadRepository.save(lead));
    }

    private LeadResponse toResponse(Lead lead) {
        return LeadResponse.builder()
                .id(lead.getId())
                .name(lead.getName())
                .email(lead.getEmail())
                .school(lead.getSchool())
                .phone(lead.getPhone())
                .message(lead.getMessage())
                .status(lead.getStatus())
                .source(lead.getSource())
                .language(lead.getLanguage())
                .createdAt(lead.getCreatedAt())
                .build();
    }
}
