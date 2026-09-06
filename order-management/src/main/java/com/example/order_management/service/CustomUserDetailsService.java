package com.example.order_management.service;

import com.example.order_management.entity.User;
import com.example.order_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Custom User Details Service.
 *
 * Implements Spring Security's UserDetailsService interface.
 * Connects Spring Security to our MySQL database by loading user records
 * and translating our Role enum into Spring Security GrantedAuthorities.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads the user by email and converts it into a Spring Security UserDetails object.
     *
     * @param email user's email address
     * @return UserDetails representation for Spring Security authentication
     * @throws UsernameNotFoundException if user does not exist in the database
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Prefix role with "ROLE_" to match Spring Security's default role conventions
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(authority)
        );
    }
}

