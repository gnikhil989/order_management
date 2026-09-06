package com.example.order_management.repository;

import com.example.order_management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * User Repository Interface.
 *
 * Provides database query operations for the User entity via Spring Data JPA.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their unique email address.
     *
     * @param email user's email
     * @return an Optional containing the User if found, empty Optional otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user already exists with the given email.
     * Generates an efficient COUNT > 0 query in MySQL.
     *
     * @param email email to verify
     * @return true if an account exists with this email, false otherwise
     */
    boolean existsByEmail(String email);
}


