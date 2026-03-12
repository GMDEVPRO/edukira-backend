package com.edukira.service;
import com.edukira.dto.request.LoginRequest;
import com.edukira.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(String refreshToken);
    void logout(String refreshToken);
}
