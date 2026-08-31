package com.orquestro.management.service;

import com.orquestro.data.domain.Language;
import com.orquestro.data.domain.User;
import com.orquestro.data.domain.enums.UserRole;
import com.orquestro.data.repository.LanguageRepository;
import com.orquestro.data.repository.UserRepository;
import com.orquestro.management.dto.request.AuthenticationRequestDTO;
import com.orquestro.management.dto.request.RegisterRequestDTO;
import com.orquestro.management.dto.response.AuthenticationResponseDTO;
import com.orquestro.management.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for managing user authentication and registration workflows.
 * It handles password encoding, token generation, and ensures data consistency
 * between identity and language preferences.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final LanguageRepository languageRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user in the platform.
     * Encodes the password, assigns a default role, and links the preferred language.
     * 
     * @param request The registration data transfer object.
     * @return AuthenticationResponseDTO containing the JWT and user details.
     * @throws RuntimeException if the email is already in use or default language is missing.
     */
    @Transactional
    public AuthenticationResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email address already in use.");
        }

        /* Resolves the user language: requested language or system default */
        Language language = languageRepository.findByCode(request.languageCode())
                .orElseGet(() -> languageRepository.findByIsDefaultTrue()
                        .orElseThrow(() -> new RuntimeException("Default language not found in system.")));

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .globalRole(UserRole.ROLE_USER) /* Default role for new registrations */
                .language(language)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);
        String jwtToken = jwtService.generateToken(savedUser);

        return mapToResponse(savedUser, jwtToken);
    }

    /**
     * Authenticates a user based on email and password.
     * Uses Spring Security's AuthenticationManager to verify credentials.
     * 
     * @param request The authentication credentials.
     * @return AuthenticationResponseDTO containing the JWT and user details.
     */
    @Transactional(readOnly = true)
    public AuthenticationResponseDTO authenticate(AuthenticationRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found after authentication."));

        String jwtToken = jwtService.generateToken(user);

        return mapToResponse(user, jwtToken);
    }

    /**
     * Internal helper to map User entity and token to the response DTO.
     */
    private AuthenticationResponseDTO mapToResponse(User user, String token) {
        return AuthenticationResponseDTO.builder()
                .accessToken(token)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .globalRole(user.getGlobalRole().name())
                .build();
    }
}