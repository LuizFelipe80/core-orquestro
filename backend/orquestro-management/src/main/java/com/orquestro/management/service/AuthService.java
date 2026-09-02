package com.orquestro.management.service;

import com.orquestro.data.domain.Language;
import com.orquestro.data.domain.User;
import com.orquestro.data.domain.UserSession;
import com.orquestro.data.domain.enums.UserRole;
import com.orquestro.data.repository.LanguageRepository;
import com.orquestro.data.repository.UserRepository;
import com.orquestro.data.repository.UserSessionRepository;
import com.orquestro.management.dto.request.AuthenticationRequestDTO;
import com.orquestro.management.dto.request.RegisterRequestDTO;
import com.orquestro.management.dto.request.TokenRefreshRequestDTO;
import com.orquestro.management.dto.response.AuthenticationResponseDTO;
import com.orquestro.management.dto.response.TokenRefreshResponseDTO;
import com.orquestro.management.exception.BusinessException;
import com.orquestro.management.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service responsible for user authentication, registration, and session management.
 * Implements advanced security features including brute force protection, 
 * account locking, and secure token rotation.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final LanguageRepository languageRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${application.security.jwt.refresh-token-expiration}")
    private long refreshExpiration;

    /**
     * Main authentication entry point. 
     * Coordinates the login process and handles security exceptions.
     * 
     * @param request the login credentials.
     * @return the authentication response with tokens.
     */
    public AuthenticationResponseDTO authenticate(AuthenticationRequestDTO request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            /* On successful authentication, reset security counters and issue tokens */
            return processSuccessfulLogin(request.email());

        } catch (BadCredentialsException e) {
            /* On failed authentication, increment counter in a separate transaction */
            updateFailedAttempts(request.email());
            throw new BusinessException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        } catch (LockedException e) {
            throw new BusinessException("This account has been locked due to multiple failed login attempts.", HttpStatus.FORBIDDEN);
        } catch (DisabledException e) {
            throw new BusinessException("This account is currently inactive.", HttpStatus.FORBIDDEN);
        }
    }

    /**
     * Increments the failed login attempt counter.
     * Uses Propagation.REQUIRES_NEW to ensure the update is committed 
     * even if the main authentication transaction rolls back.
     * 
     * @param email the user's email.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateFailedAttempts(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.isActive() && !user.isAccountLocked()) {
                user.incrementFailedAttempts();
                if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
                    user.setAccountLocked(true);
                }
                userRepository.save(user);
            }
        });
    }

    /**
     * Resets failed attempts and updates login metadata after a successful login.
     * 
     * @param email the user's email.
     * @return the complete authentication response.
     */
    @Transactional
    public AuthenticationResponseDTO processSuccessfulLogin(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found.", HttpStatus.NOT_FOUND));

        user.resetFailedAttempts();
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return createSessionAndBuildResponse(user);
    }

    /**
     * Registers a new user with standard initial security state.
     */
    @Transactional
    public AuthenticationResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email address already in use.", HttpStatus.CONFLICT);
        }

        Language language = languageRepository.findByCode(request.languageCode())
                .orElseGet(() -> languageRepository.findByIsDefaultTrue()
                        .orElseThrow(() -> new BusinessException("Default language not found.", HttpStatus.INTERNAL_SERVER_ERROR)));

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .globalRole(UserRole.ROLE_USER)
                .language(language)
                .active(true)
                .accountLocked(false)
                .failedLoginAttempts(0)
                .build();

        User savedUser = userRepository.save(user);
        return createSessionAndBuildResponse(savedUser);
    }

    /**
     * Performs secure Refresh Token Rotation.
     * Revokes the old token and issues a new pair of access/refresh tokens.
     */
    @Transactional
    public TokenRefreshResponseDTO refreshToken(TokenRefreshRequestDTO request) {
        UserSession session = userSessionRepository.findByRefreshTokenAndRevokedFalse(request.refreshToken())
                .orElseThrow(() -> new BusinessException("Invalid or revoked refresh token.", HttpStatus.UNAUTHORIZED));

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            session.setRevoked(true);
            userSessionRepository.save(session);
            throw new BusinessException("Refresh token has expired.", HttpStatus.UNAUTHORIZED);
        }

        session.setRevoked(true);
        userSessionRepository.save(session);

        User user = session.getUser();
        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = UUID.randomUUID().toString();

        saveUserSession(user, newRefreshToken);

        return TokenRefreshResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    /**
     * Voluntarily terminates a user session.
     */
    @Transactional
    public void logout(String refreshToken) {
        userSessionRepository.findByRefreshToken(refreshToken)
                .ifPresent(session -> {
                    session.setRevoked(true);
                    userSessionRepository.save(session);
                });
    }

    /**
     * Internal helper to orchestrate token generation and session persistence.
     */
    private AuthenticationResponseDTO createSessionAndBuildResponse(User user) {
        String accessToken = jwtService.generateToken(user);
        String refreshToken = UUID.randomUUID().toString();

        saveUserSession(user, refreshToken);

        return AuthenticationResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .globalRole(user.getGlobalRole().name())
                .build();
    }

    /**
     * Saves a new session record in the database for auditing and rotation.
     */
    private void saveUserSession(User user, String refreshToken) {
        UserSession session = UserSession.builder()
                .user(user)
                .refreshToken(refreshToken)
                .expiresAt(LocalDateTime.now().plusWeeks(1))
                .revoked(false)
                .build();
        userSessionRepository.save(session);
    }
}