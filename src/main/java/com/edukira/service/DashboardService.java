package com.edukira.service;

import com.edukira.dto.response.DashboardResponse;
import java.util.UUID;

public interface DashboardService {
    DashboardResponse getDashboard(UUID schoolId);
}
