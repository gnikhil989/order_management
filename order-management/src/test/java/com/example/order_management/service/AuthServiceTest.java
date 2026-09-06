package com.example.order_management.service;

import com.example.order_management.dto.request.LoginRequest;
import com.example.order_management.dto.request.RegisterRequest;
import com.example.order_management.dto.response.AuthResponse;
import com.example.order_management.entity.Role;
import com.example.order_management.entity.User;
import com.example.order_management.exception.UserAlreadyExistsException;
import com.example.order_management.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AuthService.
 * Validates registration and login logic using Mockito.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User mockUser;
    private UserDetails mockUserDetails;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .password("hashed_password")
                .role(Role.USER)
                .build();

        mockUserDetails = new org.springframework.security.core.userdetails.User(
                "john@example.com",
                "hashed_password",
                Collections.emptyList()
        );
    }

    @Test
    @DisplayName("Should successfully register a new user and return JWT token")
    void register_Success() {
        RegisterRequest registerRequest = new RegisterRequest("John Doe", "john@example.com", "Secret123!", Role.USER);

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Secret123!")).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(userDetailsService.loadUserByUsername("john@example.com")).thenReturn(mockUserDetails);
        when(jwtService.generateToken(anyMap(), eq(mockUserDetails))).thenReturn("mocked.jwt.token");
        when(jwtService.getExpirationTime()).thenReturn(86400000L);

        AuthResponse authResponse = authService.register(registerRequest);

        assertThat(authResponse).isNotNull();
        assertThat(authResponse.accessToken()).isEqualTo("mocked.jwt.token");
        assertThat(authResponse.user().email()).isEqualTo("john@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when email is already registered")
    void register_EmailAlreadyExists_ThrowsException() {
        RegisterRequest registerRequest = new RegisterRequest("John Doe", "john@example.com", "Secret123!", Role.USER);

        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should successfully authenticate user and return JWT token on login")
    void login_Success() {
        LoginRequest loginRequest = new LoginRequest("john@example.com", "Secret123!");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(mockUser));
        when(userDetailsService.loadUserByUsername("john@example.com")).thenReturn(mockUserDetails);
        when(jwtService.generateToken(anyMap(), eq(mockUserDetails))).thenReturn("mocked.jwt.token");
        when(jwtService.getExpirationTime()).thenReturn(86400000L);

        AuthResponse authResponse = authService.login(loginRequest);

        assertThat(authResponse).isNotNull();
        assertThat(authResponse.accessToken()).isEqualTo("mocked.jwt.token");
        assertThat(authResponse.user().email()).isEqualTo("john@example.com");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Should throw BadCredentialsException when login password is invalid")
    void login_InvalidPassword_ThrowsException() {
        LoginRequest loginRequest = new LoginRequest("john@example.com", "WrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Bad credentials");
    }
}
