package com.edukira.repository;
import com.edukira.entity.Payment;
import com.edukira.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Page<Payment> findBySchoolId(UUID schoolId, Pageable pageable);
    List<Payment> findBySchoolIdAndStatus(UUID schoolId, PaymentStatus status);
    Optional<Payment> findBySessionId(String sessionId);
    Optional<Payment> findByIdAndSchoolId(UUID id, UUID schoolId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.school.id = :schoolId AND p.status = 'PAID' AND p.month = :month")
    BigDecimal sumPaidBySchoolIdAndMonth(UUID schoolId, String month);

    List<Payment> findBySchoolIdAndCreatedAtAfter(UUID schoolId, Instant since);
}
