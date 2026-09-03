package com.orquestro.data.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a system user within the Orquestro platform.
 * Implements UserDetails for seamless Spring Security integration.
 * Supports multiple high-level roles and inherited granular permissions.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 180)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    /**
     * Collection of profiles assigned to the user.
     * Uses EAGER fetch type to ensure availability during JWT generation and auth checks.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_assigned_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "user_role_id")
    )
    @Builder.Default
    private Set<UserRole> roles = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Builder.Default
    @Column(name = "account_locked", nullable = false)
    private boolean accountLocked = false;

    @Builder.Default
    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts = 0;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * Brute force protection logic.
     */
    public void incrementFailedAttempts() {
        this.failedLoginAttempts++;
    }

    /**
     * Resets failed attempts after a successful login.
     */
    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
    }

    /**
     * Consolidates all authorities for the security context.
     * It flattens the hierarchy by adding 'ROLE_' prefixed UserRoles
     * and raw ModuleRole names as direct authorities.
     * 
     * @return a flattened collection of granted authorities.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();

        for (UserRole role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            
            role.getModuleRoles().forEach(moduleRole -> 
                authorities.add(new SimpleGrantedAuthority(moduleRole.getName()))
            );
        }

        return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    /**
     * Helper to get full name.
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
}