package com.edukira.repository;
import com.edukira.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    Optional<UserProfile> findByEmailAndSchoolId(String email, UUID schoolId);
    boolean existsByEmailAndSchoolId(String email, UUID schoolId);
}
