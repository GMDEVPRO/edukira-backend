package com.edukira.util;

import com.edukira.exception.EdukiraException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.UUID;

public class TenantUtil {

    private TenantUtil() {}

    @SuppressWarnings("unchecked")
    private static Map<String, String> details() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getDetails() == null) throw EdukiraException.forbidden();
        return (Map<String, String>) auth.getDetails();
    }

    public static UUID currentSchoolId() {
        String sid = details().get("schoolId");
        if (sid == null || sid.isEmpty()) throw EdukiraException.forbidden();
        return UUID.fromString(sid);
    }

    public static UUID currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw EdukiraException.forbidden();
        return UUID.fromString(auth.getPrincipal().toString());
    }

    /** ID do StudentAccount (só válido para tokens STUDENT) */
    public static UUID currentStudentAccountId() {
        return currentUserId();
    }

    /** ID da ficha Student vinculada (pode ser null se ainda não aprovado) */
    public static UUID currentStudentId() {
        String sid = details().get("studentId");
        return (sid == null || sid.isEmpty()) ? null : UUID.fromString(sid);
    }

    public static boolean isStudentToken() {
        return "STUDENT".equals(details().get("tokenType"));
    }
}
