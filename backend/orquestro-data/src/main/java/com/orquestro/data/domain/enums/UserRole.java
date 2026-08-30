package com.orquestro.data.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Defines the global access roles for the Orquestro platform.
 * These roles determine the level of administrative permission a user 
 * holds over the entire system, regardless of specific modules.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@Getter
@RequiredArgsConstructor
public enum UserRole {

    /**
     * System Administrator: Full access to all global settings, 
     * user management, module creation, and full system auditing.
     */
    ROLE_ADMIN("Administrator"),

    /**
     * System Manager: Permissions to manage existing modules and users, 
     * but with restrictions on critical system-wide configuration.
     */
    ROLE_MANAGER("Manager"),

    /**
     * Standard User: Default access. Specific functional permissions 
     * will be granted through ModuleRoles within each specific application.
     */
    ROLE_USER("User");

    /**
     * Friendly name for UI display and internationalization mapping.
     */
    private final String description;
}