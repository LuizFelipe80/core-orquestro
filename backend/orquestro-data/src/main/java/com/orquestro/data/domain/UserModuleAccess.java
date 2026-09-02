package com.orquestro.data.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the association between a User and a specific ModuleRole.
 * This entity is the core of the Granular Role-Based Access Control (RBAC),
 * defining which permissions a user holds within the context of the current module.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@Entity
@Table(
    name = "user_module_access",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_user_module_role", 
        columnNames = {"user_id", "module_role_id"}
    )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserModuleAccess extends BaseEntity {

    /**
     * The user granted with access.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The specific role assigned to the user within this module.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_role_id", nullable = false)
    private ModuleRole moduleRole;

    /**
     * Flag to enable or disable this specific access without removing the record.
     */
    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active = true;
}