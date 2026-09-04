package com.example.order_management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security Configuration.
 *
 * Configures the HTTP security filter chain, stateless session management,
 * CSRF policies, and endpoint authorization whitelists.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Endpoints that do not require any authentication (Public access).
     * Includes Swagger UI documentation assets, health check, and auth routes.
     */
    private static final String[] PUBLIC_WHITELIST = {
            // Swagger / OpenAPI endpoints
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-ui/**",
            "/swagger-ui.html",

            // System health check
            "/api/v1/health",

            // Public Authentication endpoints (Register / Login)
            "/api/v1/auth/**"
    };

    /**
     * Configures the security filter chain.
     *
     * @param http the HttpSecurity object to configure
     * @return the built SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // 1. Disable CSRF (Cross-Site Request Forgery) since REST APIs use stateless
                // JWT tokens, not browser session cookies
                .csrf(csrf -> csrf.disable())

                // 2. Configure session management to be STATELESS (no server-side HTTP session
                // created)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. Define route access rules
                .authorizeHttpRequests(auth -> auth
                        // Allow unauthenticated access to public endpoints (Swagger, Health, Auth)
                        .requestMatchers(PUBLIC_WHITELIST).permitAll()
                        // All other requests require valid authentication
                        .anyRequest().authenticated())
                .build();
    }

    /**
     * Provides a BCryptPasswordEncoder bean for securely hashing and verifying user
     * passwords.
     *
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
