package com.edukira.service.impl;

import com.edukira.dto.request.GradeBatchRequest;
import com.edukira.dto.request.GradeRequest;
import com.edukira.dto.response.ClassSummaryResponse;
import com.edukira.dto.response.GradeResponse;
import com.edukira.dto.response.ReportCardResponse;
import com.edukira.entity.Grade;
import com.edukira.entity.Student;
import com.edukira.enums.GradePeriod;
import com.edukira.exception.EdukiraException;
import com.edukira.repository.GradeRepository;
import com.edukira.repository.StudentRepository;
import com.edukira.service.GradeService;
import com.edukira.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GradeServiceImpl implements GradeService {

    private final GradeRepository   gradeRepo;
    private final StudentRepository  studentRepo;
    private final MessageService     messageService;

    @Override
    @Transactional
    public List<GradeResponse> saveBatch(GradeBatchRequest req, UUID schoolId) {
        List<GradeResponse> results = new ArrayList<>();

        for (GradeBatchRequest.GradeEntry entry : req.getGrades()) {
            Student student = studentRepo.findByIdAndSchoolId(entry.getStudentId(), schoolId)
                    .orElseThrow(() -> EdukiraException.notFound("Aluno " + entry.getStudentId()));

            // Verifica se já existe nota para este aluno/disciplina/período/ano
            Optional<Grade> existing = gradeRepo
                    .findByStudentIdAndSubjectNameAndPeriodAndYear(
                            entry.getStudentId(), req.getSubjectName(),
                            req.getPeriod(), req.getYear());

            Grade grade = existing.orElse(Grade.builder()
                    .student(student)
                    .school(student.getSchool())
                    .subjectName(req.getSubjectName())
                    .period(req.getPeriod())
                    .year(req.getYear())
                    .published(false)
                    .build());

            if (entry.getGrade1() != null)
                grade.setGrade1(BigDecimal.valueOf(entry.getGrade1()));
            if (entry.getGrade2() != null)
                grade.setGrade2(BigDecimal.valueOf(entry.getGrade2()));
            if (req.getCoefficient() != null)
                grade.setCoefficient(BigDecimal.valueOf(req.getCoefficient()));

            grade.setAverage(calcAverage(grade.getGrade1(), grade.getGrade2()));
            gradeRepo.save(grade);
            results.add(toResponse(grade));
        }

        log.info("[GRADE] Batch salvo: {} notas | disciplina={} período={} ano={}",
                results.size(), req.getSubjectName(), req.getPeriod(), req.getYear());
        return results;
    }

    @Override
    @Transactional
    public GradeResponse update(UUID gradeId, GradeRequest req, UUID schoolId) {
        Grade grade = gradeRepo.findByIdAndSchoolId(gradeId, schoolId)
                .orElseThrow(() -> EdukiraException.notFound("Nota"));

        if (req.getGrade1() != null)      grade.setGrade1(req.getGrade1());
        if (req.getGrade2() != null)      grade.setGrade2(req.getGrade2());
        if (req.getCoefficient() != null) grade.setCoefficient(req.getCoefficient());

        grade.setAverage(calcAverage(grade.getGrade1(), grade.getGrade2()));
        gradeRepo.save(grade);
        return toResponse(grade);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GradeResponse> findByClass(String classLevel, GradePeriod period,
                                           String year, UUID schoolId) {
        return gradeRepo.findBySchoolIdAndClassLevelAndPeriodAndYear(
                        schoolId, classLevel, period, year)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReportCardResponse getReportCard(UUID studentId, String year, UUID schoolId) {
        Student student = studentRepo.findByIdAndSchoolId(studentId, schoolId)
                .orElseThrow(() -> EdukiraException.notFound("Aluno"));

        List<Grade> grades = gradeRepo.findByStudentIdAndYear(studentId, year);

        Map<GradePeriod, List<ReportCardResponse.SubjectGrade>> gradesByPeriod = new LinkedHashMap<>();
        Map<GradePeriod, BigDecimal> averageByPeriod = new LinkedHashMap<>();

        for (GradePeriod period : GradePeriod.values()) {
            List<Grade> periodGrades = grades.stream()
                    .filter(g -> g.getPeriod() == period).toList();

            List<ReportCardResponse.SubjectGrade> subjects = periodGrades.stream()
                    .map(g -> ReportCardResponse.SubjectGrade.builder()
                            .subjectName(g.getSubjectName())
                            .grade1(g.getGrade1())
                            .grade2(g.getGrade2())
                            .average(g.getAverage())
                            .coefficient(g.getCoefficient())
                            .appreciation(appreciation(g.getAverage()))
                            .published(g.getPublished())
                            .build())
                    .toList();

            if (!subjects.isEmpty()) {
                gradesByPeriod.put(period, subjects);
                averageByPeriod.put(period, calcPeriodAverage(periodGrades));
            }
        }

        BigDecimal yearAverage = averageByPeriod.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(Math.max(averageByPeriod.size(), 1)), 2, RoundingMode.HALF_UP);

        return ReportCardResponse.builder()
                .studentId(student.getId())
                .studentName(student.getFirstName() + " " + student.getLastName())
                .classLevel(student.getClassLevel())
                .schoolYear(year)
                .gradesByPeriod(gradesByPeriod)
                .averageByPeriod(averageByPeriod)
                .yearAverage(yearAverage)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ClassSummaryResponse getClassSummary(String classLevel, GradePeriod period,
                                                String year, UUID schoolId) {
        List<Grade> grades = gradeRepo.findBySchoolIdAndClassLevelAndPeriodAndYear(
                schoolId, classLevel, period, year);

        // Média por disciplina
        Map<String, List<Grade>> bySubject = grades.stream()
                .collect(Collectors.groupingBy(Grade::getSubjectName));

        List<ClassSummaryResponse.SubjectAverage> subjectAverages = bySubject.entrySet().stream()
                .map(e -> {
                    List<Grade> sg = e.getValue();
                    BigDecimal avg = calcPeriodAverage(sg);
                    long pass = sg.stream().filter(g -> g.getAverage() != null
                            && g.getAverage().compareTo(BigDecimal.TEN) >= 0).count();
                    return ClassSummaryResponse.SubjectAverage.builder()
                            .subjectName(e.getKey())
                            .average(avg)
                            .passCount((int) pass)
                            .failCount(sg.size() - (int) pass)
                            .build();
                }).toList();

        // Média por aluno
        Map<UUID, List<Grade>> byStudent = grades.stream()
                .collect(Collectors.groupingBy(g -> g.getStudent().getId()));

        List<ClassSummaryResponse.StudentRank> ranks = new ArrayList<>();
        for (Map.Entry<UUID, List<Grade>> e : byStudent.entrySet()) {
            Student s = e.getValue().get(0).getStudent();
            BigDecimal avg = calcPeriodAverage(e.getValue());
            ranks.add(ClassSummaryResponse.StudentRank.builder()
                    .studentId(s.getId())
                    .studentName(s.getFirstName() + " " + s.getLastName())
                    .average(avg)
                    .build());
        }
        ranks.sort(Comparator.comparing(ClassSummaryResponse.StudentRank::getAverage).reversed());
        for (int i = 0; i < ranks.size(); i++) ranks.get(i).setRank(i + 1);

        BigDecimal classAvg = ranks.isEmpty() ? BigDecimal.ZERO
                : ranks.stream().map(ClassSummaryResponse.StudentRank::getAverage)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(ranks.size()), 2, RoundingMode.HALF_UP);

        return ClassSummaryResponse.builder()
                .classLevel(classLevel)
                .period(period)
                .year(year)
                .totalStudents(ranks.size())
                .classAverage(classAvg)
                .highestAverage(ranks.isEmpty() ? BigDecimal.ZERO : ranks.get(0).getAverage())
                .lowestAverage(ranks.isEmpty() ? BigDecimal.ZERO : ranks.get(ranks.size() - 1).getAverage())
                .subjectAverages(subjectAverages)
                .topStudents(ranks.stream().limit(5).toList())
                .build();
    }

    @Override
    @Transactional
    public int publishPeriod(String classLevel, GradePeriod period, String year, UUID schoolId) {
        List<Grade> grades = gradeRepo.findBySchoolIdAndClassLevelAndPeriodAndYear(
                        schoolId, classLevel, period, year).stream()
                .filter(g -> !Boolean.TRUE.equals(g.getPublished()))
                .toList();

        grades.forEach(g -> {
            g.setPublished(true);
            g.setPublishedAt(Instant.now());
        });
        gradeRepo.saveAll(grades);

        log.info("[GRADE] Publicadas {} notas | turma={} período={} ano={}",
                grades.size(), classLevel, period, year);
        return grades.size();
    }

    // ── Helpers ──────────────────────────────────────────────────

    private BigDecimal calcAverage(BigDecimal g1, BigDecimal g2) {
        if (g1 == null && g2 == null) return null;
        if (g1 == null) return g2;
        if (g2 == null) return g1;
        return g1.add(g2).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcPeriodAverage(List<Grade> grades) {
        List<Grade> withAvg = grades.stream()
                .filter(g -> g.getAverage() != null).toList();
        if (withAvg.isEmpty()) return BigDecimal.ZERO;
        return withAvg.stream().map(Grade::getAverage)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(withAvg.size()), 2, RoundingMode.HALF_UP);
    }

    private String appreciation(BigDecimal avg) {
        if (avg == null) return "-";
        double v = avg.doubleValue();
        if (v >= 18) return "Excellent";
        if (v >= 16) return "Très bien";
        if (v >= 14) return "Bien";
        if (v >= 12) return "Assez bien";
        if (v >= 10) return "Passable";
        return "Insuffisant";
    }

    private GradeResponse toResponse(Grade g) {
        String name = g.getStudent() != null
                ? g.getStudent().getFirstName() + " " + g.getStudent().getLastName() : "";
        return GradeResponse.builder()
                .id(g.getId())
                .studentId(g.getStudent() != null ? g.getStudent().getId() : null)
                .studentName(name)
                .subjectName(g.getSubjectName())
                .grade1(g.getGrade1())
                .grade2(g.getGrade2())
                .average(g.getAverage())
                .coefficient(g.getCoefficient())
                .period(g.getPeriod())
                .year(g.getYear())
                .published(g.getPublished())
                .publishedAt(g.getPublishedAt())
                .build();
    }
}