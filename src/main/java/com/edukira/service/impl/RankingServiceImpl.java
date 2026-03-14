package com.edukira.service.impl;

import com.edukira.dto.response.RankingResponse;
import com.edukira.entity.School;
import com.edukira.entity.SchoolRanking;
import com.edukira.enums.AttendanceStatus;
import com.edukira.enums.StudentStatus;
import com.edukira.repository.*;
import com.edukira.service.RankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RankingServiceImpl implements RankingService {

    private final SchoolRankingRepository rankingRepo;
    private final SchoolRepository        schoolRepo;
    private final StudentRepository       studentRepo;
    private final GradeRepository         gradeRepo;
    private final AttendanceRepository    attendanceRepo;
    private final PaymentRepository       paymentRepo;

    @Override
    public List<RankingResponse> getNationalRanking(String countryCode, String year, String period) {
        return rankingRepo
                .findByCountryCodeAndYearAndPeriodOrderByGlobalScoreDesc(countryCode, year, period)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<RankingResponse> getGlobalRanking(String year, String period) {
        return rankingRepo.findGlobalRanking(year, period)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public RankingResponse getSchoolRanking(UUID schoolId, String year, String period) {
        return rankingRepo.findBySchoolIdAndYearAndPeriod(schoolId, year, period)
                .map(this::toResponse)
                .orElse(null);
    }

    @Scheduled(cron = "0 0 2 * * SUN")
    public void computeRankingsScheduled() {
        computeRankings(
                String.valueOf(LocalDate.now().getYear()),
                "ANNUAL"
        );
    }

    @Override
    @Transactional
    public void computeRankings(String year, String period) {
        if (year == null) year = String.valueOf(LocalDate.now().getYear());
        if (period == null) period = "ANNUAL";

        List<School> schools = schoolRepo.findAll();
        log.info("[RANKING] Calculando rankings para {} escolas | ano={} período={}",
                schools.size(), year, period);

        for (School school : schools) {
            try {
                UUID sid = school.getId();

                long totalStudents = studentRepo.countBySchoolIdAndStatus(sid, StudentStatus.ACTIVE);
                if (totalStudents == 0) continue;

                List<com.edukira.entity.Attendance> todayAtt =
                        attendanceRepo.findBySchoolIdAndDate(sid, LocalDate.now());
                long presentToday = todayAtt.stream()
                        .filter(a -> a.getStatus() == AttendanceStatus.PRESENT
                                || a.getStatus() == AttendanceStatus.LATE).count();
                double attendanceRate = todayAtt.isEmpty() ? 85.0 :
                        Math.round((presentToday * 100.0 / todayAtt.size()) * 10.0) / 10.0;

                BigDecimal academicScore   = BigDecimal.valueOf(75.0);
                BigDecimal paymentRate     = BigDecimal.valueOf(70.0);
                BigDecimal attendanceScore = BigDecimal.valueOf(attendanceRate);
                BigDecimal globalScore     = BigDecimal.valueOf(
                        Math.round((75.0 * 0.4 + attendanceRate * 0.35 + 70.0 * 0.25) * 10.0) / 10.0);

                SchoolRanking ranking = rankingRepo
                        .findBySchoolIdAndYearAndPeriod(sid, year, period)
                        .orElse(SchoolRanking.builder()
                                .school(school)
                                .countryCode(school.getCountry())
                                .year(year)
                                .period(period)
                                .build());

                ranking.setAcademicScore(academicScore);
                ranking.setAttendanceScore(attendanceScore);
                ranking.setPaymentScore(paymentRate);
                ranking.setGlobalScore(globalScore);
                ranking.setTotalStudents((int) totalStudents);
                ranking.setPassRate(BigDecimal.valueOf(87.0));
                ranking.setAttendanceRate(BigDecimal.valueOf(attendanceRate));
                ranking.setPaymentRate(paymentRate);
                rankingRepo.save(ranking);

            } catch (Exception e) {
                log.error("[RANKING] Erro para escola={}: {}", school.getId(), e.getMessage());
            }
        }

        updateNationalRanks(year, period);
        log.info("[RANKING] Rankings calculados com sucesso!");
    }

    private void updateNationalRanks(String year, String period) {
        List<String> countries = rankingRepo.findGlobalRanking(year, period)
                .stream().map(SchoolRanking::getCountryCode).distinct().collect(Collectors.toList());

        for (String country : countries) {
            AtomicInteger rank = new AtomicInteger(1);
            rankingRepo.findByCountryCodeAndYearAndPeriodOrderByGlobalScoreDesc(country, year, period)
                    .forEach(r -> {
                        r.setNationalRank(rank.getAndIncrement());
                        rankingRepo.save(r);
                    });
        }

        AtomicInteger globalRank = new AtomicInteger(1);
        rankingRepo.findGlobalRanking(year, period).forEach(r -> {
            r.setRegionalRank(globalRank.getAndIncrement());
            rankingRepo.save(r);
        });
    }

    private RankingResponse toResponse(SchoolRanking r) {
        return RankingResponse.builder()
                .schoolId(r.getSchool().getId())
                .schoolName(r.getSchool().getName())
                .city(r.getSchool().getCity())
                .countryCode(r.getCountryCode())
                .year(r.getYear())
                .period(r.getPeriod())
                .academicScore(r.getAcademicScore())
                .attendanceScore(r.getAttendanceScore())
                .paymentScore(r.getPaymentScore())
                .globalScore(r.getGlobalScore())
                .nationalRank(r.getNationalRank())
                .regionalRank(r.getRegionalRank())
                .totalStudents(r.getTotalStudents())
                .passRate(r.getPassRate())
                .attendanceRate(r.getAttendanceRate())
                .paymentRate(r.getPaymentRate())
                .computedAt(r.getComputedAt())
                .build();
    }
}