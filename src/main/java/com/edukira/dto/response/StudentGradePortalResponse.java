package com.edukira.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data @Builder
public class StudentGradePortalResponse {
    private String period;
    private String year;
    private String classAverage;
    private String studentAverage;
    private String rank;
    private List<SubjectGrade> subjects;

    @Data @Builder
    public static class SubjectGrade {
        private String subject;
        private String grade1;
        private String grade2;
        private String average;
        private String coefficient;
        private String appreciation;
    }
}
