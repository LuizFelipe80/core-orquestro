package com.orquestro.data.repository;

import com.orquestro.data.domain.User;
import com.orquestro.data.domain.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for UserSession entity operations.
 * Handles session persistence, refresh token validation, and session management.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    /**
     * Finds a session by its refresh token string.
     * 
     * @param refreshToken the token string to search for.
     * @return an Optional containing the found session.
     */
    Optional<UserSession> findByRefreshToken(String refreshToken);

    /**
     * Finds an active (non-revoked) session by its refresh token.
     * Used during the token refresh process to ensure the session is still valid.
     * 
     * @param refreshToken the token string to check.
     * @return an Optional containing the session if it exists and is not revoked.
     */
    Optional<UserSession> findByRefreshTokenAndRevokedFalse(String refreshToken);

    /**
     * Retrieves all active (non-revoked) sessions for a specific user.
     * Essential for security features like "View active devices" or "Logout from all devices".
     * 
     * @param user the user whose active sessions should be retrieved.
     * @return a list of active user sessions.
     */
    List<UserSession> findAllByUserAndRevokedFalse(User user);
}