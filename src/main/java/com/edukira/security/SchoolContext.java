package com.edukira.security;

import com.edukira.exception.EdukiraException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.UUID;

public class SchoolContext {

    public static UUID getSchoolId() {
        Map<String, String> details = getDetails();
        String schoolId = details.get("schoolId");
        if (schoolId == null || schoolId.isBlank()) {
            throw new EdukiraException("schoolId não encontrado no token", HttpStatus.UNAUTHORIZED);
        }
        return UUID.fromString(schoolId);
    }

    public static UUID getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new EdukiraException("Utilizador não autenticado", HttpStatus.UNAUTHORIZED);
        }
        try {
            return UUID.fromString(auth.getName());
        } catch (Exception e) {
            return null;
        }
    }

    public static UUID getStudentId() {
        Map<String, String> details = getDetails();
        String studentId = details.get("studentId");
        if (studentId == null || studentId.isBlank()) return null;
        return UUID.fromString(studentId);
    }

    public static boolean isStudent() {
        Map<String, String> details = getDetails();
        return "STUDENT".equals(details.get("tokenType"));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> getDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof UsernamePasswordAuthenticationToken token) {
            if (token.getDetails() instanceof Map) {
                return (Map<String, String>) token.getDetails();
            }
        }
        throw new EdukiraException("Detalhes do token não encontrados", HttpStatus.UNAUTHORIZED);
    }
}