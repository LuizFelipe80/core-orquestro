package com.orquestro.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.orquestro.management.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

/**
 * Main security configuration class for the Orquestro platform.
 * Defines the security filter chain, URL permissions, CORS settings, 
 * and integrates the JWT authentication mechanism.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    /**
     * Configures the security filter chain.
     * 
     * @param http the HttpSecurity object to configure.
     * @return the built SecurityFilterChain.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            /* Disables CSRF as we are using stateless JWT tokens */
            .csrf(AbstractHttpConfigurer::disable)
            
            /* Configures CORS with the settings defined in corsConfigurationSource() */
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            /* Defines access rules for HTTP requests */
            .authorizeHttpRequests(auth -> auth
                /* Public endpoints for authentication and initial setup */
            		.requestMatchers(
            				"/auth/authenticate", 
            		        "/auth/refresh", 
            		        "/auth/logout",
            		        "/v3/api-docs/**",
            		        "/swagger-ui/**",
            		        "/swagger-ui.html",
            		        "/actuator/health/**"
            		    ).permitAll()
            		    
            		    /* Administrative endpoints */
            		    .requestMatchers("/auth/register").hasRole("ADMINISTRATOR")
                /* All other requests must be authenticated */
                .anyRequest().authenticated()
            )
            
            /* Sets the session management to stateless */
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            /* Sets the custom authentication provider defined in ApplicationConfig */
            .authenticationProvider(authenticationProvider)
            
            /* Adds our JWT filter before the standard UsernamePasswordAuthenticationFilter */
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configures Cross-Origin Resource Sharing (CORS) to allow 
     * communication between the Frontend (React) and the Backend.
     * 
     * @return the configured CorsConfigurationSource.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        /* In production, these should be restricted to specific domains */
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept-Language"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        /* Applies this configuration to all endpoints */
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}