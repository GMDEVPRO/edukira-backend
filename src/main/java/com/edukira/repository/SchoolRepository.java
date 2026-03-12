package com.edukira.repository;
import com.edukira.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SchoolRepository extends JpaRepository<School, UUID> {
    Optional<School> findByIdAndActiveTrue(UUID id);
}
