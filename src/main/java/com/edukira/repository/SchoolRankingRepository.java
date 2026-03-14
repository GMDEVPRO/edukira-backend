package com.edukira.repository;

import com.edukira.entity.SchoolRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SchoolRankingRepository extends JpaRepository<SchoolRanking, UUID> {

    List<SchoolRanking> findByCountryCodeAndYearAndPeriodOrderByGlobalScoreDesc(
            String countryCode, String year, String period);

    Optional<SchoolRanking> findBySchoolIdAndYearAndPeriod(
            UUID schoolId, String year, String period);

    @Query("SELECT r FROM SchoolRanking r WHERE r.year = :year " +
            "AND r.period = :period ORDER BY r.globalScore DESC")
    List<SchoolRanking> findGlobalRanking(String year, String period);
}
