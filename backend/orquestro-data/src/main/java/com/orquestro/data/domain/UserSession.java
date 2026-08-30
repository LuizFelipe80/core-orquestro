package com.orquestro.data.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Represents an active user session in the Orquestro platform.
 * This entity is used to manage Refresh Tokens, track user devices, 
 * and allow for remote session revocation for enhanced security.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@Entity
@Table(name = "user_sessions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSession extends BaseEntity {

    /**
     * The user associated with this session.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Unique Refresh Token used to obtain new Access Tokens.
     * This should be a long, cryptographically secure random string.
     */
    @Column(name = "refresh_token", nullable = false, unique = true, length = 500)
    private String refreshToken;

    /**
     * Metadata about the user's browser and operating system.
     */
    @Column(name = "user_agent")
    private String userAgent;

    /**
     * The IP address from which the session was initiated.
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * Expiration date and time for the Refresh Token.
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Flag to manually revoke the session before its natural expiration.
     */
    @Builder.Default
    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;

    /**
     * Checks if the session is still valid based on expiration date and revocation status.
     * 
     * @return true if the session is active and not expired.
     */
    public boolean isValid() {
        return !revoked && LocalDateTime.now().isBefore(expiresAt);
    }
}