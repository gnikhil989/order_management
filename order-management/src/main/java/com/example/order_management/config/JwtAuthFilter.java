package com.example.order_management.config;

import com.example.order_management.service.CustomUserDetailsService;
import com.example.order_management.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter.
 *
 * Runs once per HTTP request to inspect the "Authorization" header.
 * If a valid JWT Bearer token is found, it populates the Spring SecurityContext
 * with the authenticated user's identity and roles.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Step 1: Check if the Authorization header is present and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Step 2: Extract the raw JWT token (substring after "Bearer ")
        jwt = authHeader.substring(7);

        try {
            // Step 3: Extract the user email from the token
            userEmail = jwtService.extractUsername(jwt);

            // Step 4: If email exists and user is not already authenticated in the current SecurityContext
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // Step 5: Validate the token signature and expiration
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Step 6: Store authenticated user in Spring's SecurityContextHolder
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Authenticated user {} for URI: {}", userEmail, request.getRequestURI());
                }
            }
        } catch (Exception e) {
            log.error("Failed to authenticate JWT token: {}", e.getMessage());
            // Do not throw here; let Spring Security authorization filter handle unauthenticated requests
        }

        // Step 7: Continue the filter chain
        filterChain.doFilter(request, response);
    }
}

