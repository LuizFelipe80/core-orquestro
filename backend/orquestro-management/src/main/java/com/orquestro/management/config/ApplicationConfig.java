package com.orquestro.management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.orquestro.data.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Central configuration class for application-wide security beans.
 * This class defines how the system retrieves user information, handles 
 * password encoding, and manages authentication providers.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserRepository userRepository;

    /**
     * Defines how Spring Security should find the user in our database.
     * Uses the email as the unique identifier for authentication.
     * 
     * @return an implementation of UserDetailsService.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

    /**
     * Configures the data access object (DAO) authentication provider.
     * Links our custom userDetailsService and passwordEncoder to the security engine.
     * 
     * @return the configured AuthenticationProvider.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Exposes the AuthenticationManager from Spring's configuration.
     * Required for manual authentication triggers in the service layer.
     * 
     * @param config provided by Spring Security.
     * @return the AuthenticationManager instance.
     * @throws Exception if configuration fails.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Defines the password hashing algorithm for the entire system.
     * BCrypt is the industry standard for secure password storage.
     * 
     * @return an instance of BCryptPasswordEncoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}