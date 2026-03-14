package com.edukira.service;

import com.edukira.dto.request.SchoolRegistrationRequest;
import com.edukira.dto.response.SchoolRegistrationResponse;

public interface SchoolRegistrationService {
    SchoolRegistrationResponse register(SchoolRegistrationRequest request);
}
