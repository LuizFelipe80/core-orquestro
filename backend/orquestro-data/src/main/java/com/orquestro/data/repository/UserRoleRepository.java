package com.orquestro.data.repository;

import com.orquestro.data.domain.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for UserRole entity operations.
 * Manages high-level access profiles that aggregate granular module permissions.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    /**
     * Finds a user role profile by its unique name.
     * 
     * @param name the name of the profile (e.g., 'ADMINISTRATOR', 'USER').
     * @return an Optional containing the found user role.
     */
    Optional<UserRole> findByName(String name);

    /**
     * Checks if a user role exists with the given name.
     * 
     * @param name the name to verify.
     * @return true if the role name is already registered.
     */
    boolean existsByName(String name);
}