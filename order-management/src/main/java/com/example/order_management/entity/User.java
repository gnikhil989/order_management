package com.example.order_management.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * User Entity.
 *
 * Represents an authenticated user (Customer or Admin) in the system.
 * Maps to the "users" table in MySQL.
 */
@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    /**
     * Primary key auto-increment identifier (PostgreSQL Identity / MySQL AUTO_INCREMENT).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User's full name.
     */
    @Column(nullable = false)
    private String name;

    /**
     * User's email address (used as the login username).
     * Must be unique across all accounts.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * BCrypt hashed password.
     * Never stored as plain text.
     */
    @Column(nullable = false)
    private String password;

    /**
     * Role determining user authorization and API permissions.
     * Stored as a VARCHAR string in the database rather than an ordinal integer.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * Timestamp when the user record was first created.
     * Automatically populated by Hibernate upon entity creation.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the user record was last updated.
     * Automatically updated by Hibernate upon entity modification.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
