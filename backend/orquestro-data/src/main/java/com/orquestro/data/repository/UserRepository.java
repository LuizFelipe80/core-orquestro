package com.orquestro.data.repository;

import com.orquestro.data.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
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
     * Finds a user by their email address.
     * This is primarily used during the authentication process.
     * 
     * @param email the user's email.
     * @return an Optional containing the found user, or empty if not found.
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user exists with the given email.
     * Used for validation during user registration.
     * 
     * @param email the email to check.
     * @return true if the email is already in use.
     */
    boolean existsByEmail(String email);
}