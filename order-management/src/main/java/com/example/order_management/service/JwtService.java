package com.example.order_management.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT Service.
 *
 * Dedicated service responsible solely for JSON Web Token operations:
 * - Generating signed tokens with custom claims (user ID, roles, email).
 * - Extracting claims and username from tokens.
 * - Validating token signature and expiration against user details.
 *
 * Adheres to Single Responsibility Principle (SRP).
 */
@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    /**
     * Extracts the username (email) stored as the 'Subject' of the token.
     *
     * @param token the raw JWT token
     * @return username / email string
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Generic helper to extract a specific claim from the token payload.
     *
     * @param token raw JWT token
     * @param claimsResolver functional mapper to extract desired field
     * @param <T> return type of the claim
     * @return extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Generates a JWT token for the given user with standard subject and expiration.
     *
     * @param userDetails authenticated user details
     * @return signed JWT string
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generates a JWT token containing extra custom claims (e.g. role, user ID).
     *
     * @param extraClaims map of additional claims to include in the payload
     * @param userDetails authenticated user details
     * @return signed JWT string
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Validates whether a token belongs to the given user and has not expired.
     *
     * @param token raw JWT token
     * @param userDetails user details to verify against
     * @return true if valid and unexpired, false otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Checks if the token expiration timestamp has passed.
     *
     * @param token raw JWT token
     * @return true if expired, false if still active
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the expiration date from the token.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Parses and verifies the JWT signature, returning all decoded claims.
     *
     * @param token raw JWT token
     * @return decoded Claims body
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Decodes the configured secret key into a cryptographic HMAC SHA SecretKey object.
     *
     * @return SecretKey instance
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Returns the configured token expiration time in milliseconds.
     */
    public long getExpirationTime() {
        return jwtExpiration;
    }
}

