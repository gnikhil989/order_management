package com.example.order_management.controller;

import com.example.order_management.config.JwtAuthFilter;
import com.example.order_management.dto.request.LoginRequest;
import com.example.order_management.dto.request.RegisterRequest;
import com.example.order_management.dto.response.AuthResponse;
import com.example.order_management.dto.response.UserResponse;
import com.example.order_management.entity.Role;
import com.example.order_management.service.AuthService;
import com.example.order_management.service.CustomUserDetailsService;
import com.example.order_management.service.JwtService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller unit tests for AuthController.
 * Validates HTTP routing, JSON serialization, and @Valid bean validation constraints.
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters to test controller validation and mapping in isolation
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("POST /api/v1/auth/register - Should return 201 Created on valid input")
    void register_ValidPayload_Returns201() throws Exception {
        RegisterRequest request = new RegisterRequest("Jane Doe", "jane@example.com", "Password123!", Role.USER);
        UserResponse userResponse = new UserResponse(UUID.randomUUID(), "Jane Doe", "jane@example.com", Role.USER, LocalDateTime.now());
        AuthResponse authResponse = new AuthResponse("sample.jwt.token", 86400000L, userResponse);

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("sample.jwt.token"))
                .andExpect(jsonPath("$.user.email").value("jane@example.com"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Should return 400 Bad Request on invalid email")
    void register_InvalidEmail_Returns400() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest("Jane Doe", "not-a-valid-email", "pass", Role.USER);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Should return 200 OK on valid credentials")
    void login_ValidPayload_Returns200() throws Exception {
        LoginRequest request = new LoginRequest("jane@example.com", "Password123!");
        UserResponse userResponse = new UserResponse(UUID.randomUUID(), "Jane Doe", "jane@example.com", Role.USER, LocalDateTime.now());
        AuthResponse authResponse = new AuthResponse("sample.jwt.token", 86400000L, userResponse);

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("sample.jwt.token"))
                .andExpect(jsonPath("$.user.email").value("jane@example.com"));
    }
}

