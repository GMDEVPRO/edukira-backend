package com.edukira.repository;

import com.edukira.entity.NotificationLog;
import com.edukira.enums.NotificationChannel;
import com.edukira.enums.NotificationStatus;
import com.edukira.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {

    Page<NotificationLog> findBySchoolId(UUID schoolId, Pageable pageable);

    Page<NotificationLog> findBySchoolIdAndType(UUID schoolId, NotificationType type, Pageable pageable);

    Page<NotificationLog> findBySchoolIdAndStatus(UUID schoolId, NotificationStatus status, Pageable pageable);

    Page<NotificationLog> findBySchoolIdAndChannel(UUID schoolId, NotificationChannel channel, Pageable pageable);

    // Para retry: busca falhas com menos de 3 tentativas
    @Query("SELECT n FROM NotificationLog n WHERE n.status = 'FAILED' AND n.retryCount < 3")
    List<NotificationLog> findRetryable();

    long countBySchoolIdAndStatus(UUID schoolId, NotificationStatus status);

    long countBySchoolIdAndType(UUID schoolId, NotificationType type);
}
