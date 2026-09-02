package com.orquestro.data.repository;

import com.orquestro.data.domain.ModuleRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for ModuleRole entity operations.
 * Handles persistence and lookup for business-specific roles within the module.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@Repository
public interface ModuleRoleRepository extends JpaRepository<ModuleRole, UUID> {

    /**
     * Finds a module role by its unique name.
     * 
     * @param name the role name (e.g., 'OPERATOR').
     * @return an Optional containing the found role.
     */
    Optional<ModuleRole> findByName(String name);

    /**
     * Retrieves all module roles that are currently active.
     * Used to populate role assignment selectors in the UI.
     * 
     * @return a list of active module roles.
     */
    List<ModuleRole> findAllByActiveTrue();

    /**
     * Checks if a role with the given name already exists.
     * 
     * @param name the role name to check.
     * @return true if the name is already in use.
     */
    boolean existsByName(String name);
}