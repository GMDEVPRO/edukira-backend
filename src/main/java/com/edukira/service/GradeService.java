package com.edukira.service;

import com.edukira.dto.request.GradeBatchRequest;
import com.edukira.dto.request.GradeRequest;
import com.edukira.dto.response.ClassSummaryResponse;
import com.edukira.dto.response.GradeResponse;
import com.edukira.dto.response.ReportCardResponse;
import com.edukira.enums.GradePeriod;

import java.util.List;
import java.util.UUID;

public interface GradeService {
    List<GradeResponse> saveBatch(GradeBatchRequest request, UUID schoolId);

    GradeResponse update(UUID gradeId, GradeRequest request, UUID schoolId);

    List<GradeResponse> findByClass(String classLevel, GradePeriod period, String year, UUID schoolId);

    ReportCardResponse getReportCard(UUID studentId, String year, UUID schoolId);

    ClassSummaryResponse getClassSummary(String classLevel, GradePeriod period, String year, UUID schoolId);

    int publishPeriod(String classLevel, GradePeriod period, String year, UUID schoolId);
}
