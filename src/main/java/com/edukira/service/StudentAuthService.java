package com.edukira.service;

import com.edukira.dto.request.StudentLoginRequest;
import com.edukira.dto.request.StudentRegisterRequest;
import com.edukira.dto.response.StudentAuthResponse;

public interface StudentAuthService {
    StudentAuthResponse register(StudentRegisterRequest request);
    StudentAuthResponse login(StudentLoginRequest request);
    void logout(String refreshToken);
}
