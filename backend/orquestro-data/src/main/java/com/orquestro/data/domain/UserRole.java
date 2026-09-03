package com.orquestro.data.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a high-level access profile within the Orquestro platform.
 * UserRoles act as containers for granular ModuleRoles. 
 * The moduleRoles collection is loaded EAGERLY to ensure permissions are 
 * always available during security authorization checks.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@Entity
@Table(name = "user_roles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRole extends BaseEntity {

    /**
     * The unique name of the role (e.g., "ADMINISTRATOR", "MANAGER").
     */
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    /**
     * A descriptive text for the profile's intended use.
     */
    @Column(name = "description", length = 255)
    private String description;

    /**
     * Flag indicating if this role is currently active.
     */
    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active = true;

    /**
     * The set of granular module-specific roles associated with this profile.
     * Uses EAGER fetch type to prevent session-related errors during auth flattening.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_role_module_mapping",
        joinColumns = @JoinColumn(name = "user_role_id"),
        inverseJoinColumns = @JoinColumn(name = "module_role_id")
    )
    @Builder.Default
    private Set<ModuleRole> moduleRoles = new HashSet<>();
}