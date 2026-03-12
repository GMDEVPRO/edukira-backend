package com.edukira.dto.response;

import com.edukira.enums.AttendanceStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class AttendanceResponse {
    private UUID id;
    private UUID studentId;
    private String studentName;
    private String classLevel;
    private LocalDate date;
    private String subject;
    private AttendanceStatus status;
    private String observation;
}
