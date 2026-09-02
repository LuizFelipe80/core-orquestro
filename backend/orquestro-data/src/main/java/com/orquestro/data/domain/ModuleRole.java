package com.orquestro.data.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a functional role within a specific application module.
 * Unlike global UserRoles, ModuleRoles are used to define granular permissions
 * related to business logic (e.g., Production Operator, HR Manager).
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@Entity
@Table(name = "module_roles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleRole extends BaseEntity {

    /**
     * The unique name of the role (e.g., "OEE_ANALYST", "MACHINE_OPERATOR").
     * Should be in uppercase and follow a standard naming convention.
     */
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    /**
     * A brief description of what this role is allowed to do in the module.
     */
    @Column(name = "description", length = 255)
    private String description;

    /**
     * Flag indicating if this role is currently active and can be assigned to users.
     */
    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active = true;
}