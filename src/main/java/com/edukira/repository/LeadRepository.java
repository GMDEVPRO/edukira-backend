package com.edukira.repository;


import com.edukira.entity.Lead;
import com.edukira.enums.LeadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LeadRepository extends JpaRepository<Lead, UUID> {
    List<Lead> findByStatusOrderByCreatedAtDesc(LeadStatus status);
    boolean existsByEmail(String email);
}