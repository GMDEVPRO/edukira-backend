package com.edukira.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    private static final String[] PUBLIC_URLS = {
            // Staff auth
            "/v1/auth/**",
            // Student auth (registo e login públicos)
            "/v1/student/auth/register",
            "/v1/student/auth/login",
            "/v1/student/auth/logout",
            // Webhooks Mobile Money
            "/v1/payments/webhook",
            // Docs
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/health"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_URLS).permitAll()
                        // Portal do aluno — só STUDENT
                        .requestMatchers("/v1/student/portal/me/**").hasRole("STUDENT")
                        .requestMatchers("/v1/student/portal/grades").hasRole("STUDENT")
                        .requestMatchers("/v1/student/portal/payments").hasRole("STUDENT")
                        .requestMatchers("/v1/student/portal/documents").hasRole("STUDENT")
                        // Gestão de contas de aluno — só admins
                        .requestMatchers("/v1/student/portal/admin/**").hasAnyRole("SCHOOL_ADMIN","ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
