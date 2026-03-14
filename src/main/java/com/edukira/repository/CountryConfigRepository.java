package com.edukira.repository;

import com.edukira.entity.CountryConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CountryConfigRepository extends JpaRepository<CountryConfig, String> {
    List<CountryConfig> findByActiveTrue();
}
