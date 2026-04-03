package com.edukira.repository;

import com.edukira.entity.Subscription;
import com.edukira.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    Optional<Subscription> findBySchoolId(UUID schoolId);

    boolean existsBySchoolId(UUID schoolId);

    // Busca trials que expiram em menos de 3 dias (para avisos)
    @Query("SELECT s FROM Subscription s WHERE s.status = 'TRIAL' AND s.trialEndsAt < :threshold")
    List<Subscription> findExpiringTrials(Instant threshold);

    // Busca assinaturas activas que expiraram (para desactivar via scheduler)
    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.currentPeriodEnd < :now")
    List<Subscription> findExpiredActive(Instant now);
}
