package com.orquestro.data.repository;

import com.orquestro.data.domain.User;
import com.orquestro.data.domain.UserModuleAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for UserModuleAccess entity operations.
 * This is the primary data access point for determining user permissions 
 * within the specific application module.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@Repository
public interface UserModuleAccessRepository extends JpaRepository<UserModuleAccess, UUID> {

    /**
     * Retrieves all active access records for a specific user.
     * Essential for building the user's authority list during the authentication process.
     * 
     * @param user the user whose module permissions are being requested.
     * @return a list of active UserModuleAccess records.
     */
    List<UserModuleAccess> findAllByUserAndActiveTrue(User user);

    /**
     * Retrieves all access assignments for a user, regardless of active status.
     * Used in administrative screens to manage a user's roles.
     * 
     * @param userId the unique identifier of the user.
     * @return a list of all UserModuleAccess records for the user.
     */
    List<UserModuleAccess> findAllByUserId(UUID userId);

    /**
     * Checks if a user has a specific role assigned and active.
     * 
     * @param userId the unique identifier of the user.
     * @param roleName the name of the module role (e.g., 'OEE_ANALYST').
     * @return true if the active assignment exists.
     */
    boolean existsByUserIdAndModuleRoleNameAndActiveTrue(UUID userId, String roleName);
}