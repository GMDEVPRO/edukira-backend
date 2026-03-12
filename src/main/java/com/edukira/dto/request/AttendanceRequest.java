package com.edukira.dto.request;

import com.edukira.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class AttendanceRequest {

    @NotNull
    private LocalDate date;

    private String subject;

    @NotNull
    private List<AttendanceEntry> entries;

    @Data
    public static class AttendanceEntry {
        @NotNull
        private UUID studentId;
        @NotNull
        private AttendanceStatus status;
        private String observation;
    }
}