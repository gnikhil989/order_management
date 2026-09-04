# Guide 03: Spring Security 6 Architecture & Base Configuration

> **Purpose**: This guide covers the **Spring Security 6** architecture, explaining how the filter chain operates, why we disable CSRF, how stateless sessions work, and how route whitelisting is structured.

---

## 1. Spring Security 6 vs Legacy Spring Security

In Spring Boot 3+, Spring Security was modernized with major changes:
* `WebSecurityConfigurerAdapter` is **completely removed**.
* Configuration is now **component-based / bean-based** via `SecurityFilterChain`.
* Functional lambda DSL style (e.g., `csrf -> csrf.disable()`) is standard.

---

## 2. Security Filter Chain Architecture

Spring Security works as a series of **Servlet Filters** executed in order before any request hits your `@RestController`:

```
Incoming HTTP Request
       │
       ▼
┌────────────────────────────────────────────────────────┐
│               SPRING SECURITY FILTER CHAIN             │
│                                                        │
│  1. SecurityContextPersistence / Session Filter        │
│  2. CSRF Filter (Disabled for REST)                    │
│  3. Custom Filters (e.g., RateLimitFilter, JwtAuthFilter)│
│  4. UsernamePasswordAuthenticationFilter               │
│  5. AuthorizationFilter (checks permitAll / roles)     │
└──────────────────────────┬─────────────────────────────┘
                           │ If Authorized
                           ▼
                 DispatcherServlet ──► Your @RestController
```

---

## 3. Base Security Configuration Explained (`SecurityConfig.java`)

File: [`src/main/java/com/example/order_management/config/SecurityConfig.java`](../src/main/java/com/example/order_management/config/SecurityConfig.java)

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_WHITELIST = {
            // Swagger / OpenAPI documentation endpoints
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-ui/**",
            "/swagger-ui.html",
            
            // Health check and public authentication endpoints
            "/api/v1/health",
            "/api/v1/auth/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // 1. Disable CSRF (Cross-Site Request Forgery)
                .csrf(csrf -> csrf.disable())
                
                // 2. Set Stateless Session Management
                .sessionManagement(session -> 
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // 3. Define URL Authorization Rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_WHITELIST).permitAll()
                        .anyRequest().authenticated()
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

---

## 4. Deep-Dive: Why These Specific Configurations?

### 1. Why `csrf.disable()`?
* **CSRF (Cross-Site Request Forgery)** attacks exploit browser cookies sent automatically with form submissions.
* In our REST API, authentication is handled via **JWT Bearer tokens stored by the client and sent in HTTP headers**, not via session cookies.
* Therefore, CSRF protection is unnecessary and would incorrectly reject valid `POST`/`PUT`/`DELETE` API requests.

### 2. Why `SessionCreationPolicy.STATELESS`?
* By default, Spring Security creates an `HttpSession` in memory for every logged-in user.
* In high-scale distributed systems, servers should be **stateless** (so any server instance behind a load balancer can handle any request using the JWT).
* `STATELESS` tells Spring: **never create or use an HTTP session to store the SecurityContext**. Every request must authenticate itself independently.

### 3. Why `BCryptPasswordEncoder`?
* Passwords must **never** be stored as plain text or simple hashes (like MD5 or SHA-256).
* `BCrypt` includes a random per-password salt and an adaptive slow work factor, making rainbow table and brute-force attacks mathematically infeasible.

---

## 5. Upcoming Security Roadmap

In subsequent steps, we will expand this configuration by adding:
1. **`JwtService`**: To generate, sign, and validate JWT tokens.
2. **`UserDetailsService`**: To load user details and roles from our MySQL database.
3. **`JwtAuthFilter`**: A custom `OncePerRequestFilter` added into the filter chain before `UsernamePasswordAuthenticationFilter`.
4. **Role-Based Access Control**: Using `@PreAuthorize("hasRole('ADMIN')")` for restricted actions.
