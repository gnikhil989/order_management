package com.example.order_management.service;

import com.example.order_management.dto.request.LoginRequest;
import com.example.order_management.dto.request.RegisterRequest;
import com.example.order_management.dto.response.AuthResponse;
import com.example.order_management.dto.response.UserResponse;
import com.example.order_management.entity.User;
import com.example.order_management.exception.UserAlreadyExistsException;
import com.example.order_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Service.
 *
 * Implements business logic for registering new users and authenticating existing users.
 * Follows DRY principles by centralizing JWT claim building and authentication responses.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user account.
     *
     * 1. Checks if the email is already registered.
     * 2. Encrypts the plain-text password using BCrypt.
     * 3. Saves the User entity to MySQL.
     * 4. Issues a signed JWT access token.
     *
     * @param request validated registration payload
     * @return AuthResponse containing the JWT token and safe user details
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Attempting to register new user with email: {}", request.email());

        // Step 1: Ensure email uniqueness
        if (userRepository.existsByEmail(request.email())) {
            log.warn("Registration failed: email {} already exists", request.email());
            throw new UserAlreadyExistsException("An account with email " + request.email() + " already exists.");
        }

        // Step 2: Build new User entity with hashed password
        User user = User.builder()
                .name(request.name().trim())
                .email(request.email().trim().toLowerCase())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .build();

        // Step 3: Persist in MySQL database
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        // Step 4: Generate JWT token and return response DTO (DRY helper)
        return generateAuthResponse(savedUser);
    }

    /**
     * Authenticates an existing user and issues a fresh JWT access token.
     *
     * 1. Uses Spring Security's AuthenticationManager to verify credentials.
     * 2. Retrieves the user record from the database.
     * 3. Issues a signed JWT token.
     *
     * @param request validated login credentials
     * @return AuthResponse containing the JWT token and user profile
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("User login attempt for email: {}", request.email());

        // Step 1: Authenticate credentials using Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email().trim().toLowerCase(),
                        request.password()
                )
        );

        // Step 2: Fetch user record from database
        User user = userRepository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new IllegalStateException("User not found after successful authentication"));

        log.info("Login successful for user: {}", user.getEmail());

        // Step 3: Generate JWT token and return response DTO (DRY helper)
        return generateAuthResponse(user);
    }

    /**
     * DRY Helper: Generates a signed JWT token with custom claims and maps to AuthResponse.
     *
     * @param user the authenticated User entity
     * @return populated AuthResponse DTO
     */
    private AuthResponse generateAuthResponse(User user) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId().toString());
        extraClaims.put("role", user.getRole().name());

        String jwtToken = jwtService.generateToken(extraClaims, userDetails);

        return new AuthResponse(
                jwtToken,
                jwtService.getExpirationTime(),
                UserResponse.fromEntity(user)
        );
    }
}


