package com.edukira.repository;
<<<<<<< HEAD

=======
>>>>>>> 94be4867219629388a5124e0c6675443891a295c
import com.edukira.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< HEAD

import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    Page<Message> findBySchoolId(UUID schoolId, Pageable pageable);

    Page<Message> findBySchoolIdOrderBySentAtDesc(UUID schoolId, Pageable pageable);

    Optional<Message> findByIdAndSchoolId(UUID id, UUID schoolId);
}
=======
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    Page<Message> findBySchoolIdOrderBySentAtDesc(UUID schoolId, Pageable pageable);
}
>>>>>>> 94be4867219629388a5124e0c6675443891a295c
