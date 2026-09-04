package com.orquestro.data.repository;

import com.orquestro.data.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User entity operations.
 * Provides standard CRUD operations and custom query methods for authentication.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Finds a user by their email address with eager fetching of relations.
     * This is primarily used during the authentication process.
     * 
     * @param email the user's email.
     * @return an Optional containing the found user, or empty if not found.
     */
    @EntityGraph(attributePaths = {"language", "roles", "roles.moduleRoles"})
    Optional<User> findByEmail(String email);

    /**
     * Retrieves a paginated list of users with language and roles eagerly fetched
     * to eliminate N+1 query overhead.
     */
    @Override
    @NonNull
    @EntityGraph(attributePaths = {"language", "roles", "roles.moduleRoles"})
    Page<User> findAll(@NonNull Pageable pageable);

    /**
     * Finds a user by ID with all relations eagerly fetched.
     */
    @Override
    @NonNull
    @EntityGraph(attributePaths = {"language", "roles", "roles.moduleRoles"})
    Optional<User> findById(@NonNull UUID id);

    /**
     * Checks if a user exists with the given email.
     * Used for validation during user registration.
     * 
     * @param email the email to check.
     * @return true if the email is already in use.
     */
    boolean existsByEmail(String email);

    /**
     * Checks if any user is currently associated with a given language.
     * Used to prevent deleting languages that are in active use.
     * 
     * @param languageId the language UUID.
     * @return true if at least one user is assigned to this language.
     */
    boolean existsByLanguageId(UUID languageId);
}