package com.edukira.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (jwtService.isTokenValid(token)) {
            var claims = jwtService.extractAllClaims(token);
            String role = claims.get("role", String.class);
            String schoolId = claims.get("schoolId", String.class);
            String tokenType = claims.get("tokenType", String.class);

            // Details map: schoolId + extra info for student tokens
            Map<String, String> details = new HashMap<>();
            details.put("schoolId", schoolId);
            details.put("tokenType", tokenType != null ? tokenType : "STAFF");

            if ("STUDENT".equals(tokenType)) {
                String studentId = claims.get("studentId", String.class);
                if (studentId != null) details.put("studentId", studentId);
            }

            var auth = new UsernamePasswordAuthenticationToken(
                    claims.getSubject(),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );
            auth.setDetails(details);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}
