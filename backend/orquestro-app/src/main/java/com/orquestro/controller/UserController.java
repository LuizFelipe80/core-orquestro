package com.orquestro.controller;

import com.orquestro.management.dto.request.UserUpdateDTO;
import com.orquestro.management.dto.response.UserResponseDTO;
import com.orquestro.management.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for administrative user management operations.
 * Provides endpoints for listing, viewing, updating, and managing account security status.
 * Access is restricted to users with ADMIN or MANAGER global roles.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'MANAGER')")
public class UserController {

    private final UserService userService;

    /**
     * Retrieves a paginated list of all users.
     * 
     * @param pageable pagination and sorting information.
     * @return a page of UserResponseDTOs.
     */
    @GetMapping
    public ResponseEntity<Page<UserResponseDTO>> getAllUsers(
            @PageableDefault(sort = "firstName", size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(userService.findAll(pageable));
    }

    /**
     * Retrieves detailed information about a specific user.
     * 
     * @param id the unique identifier of the user.
     * @return the UserResponseDTO.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    /**
     * Updates an existing user's profile information.
     * 
     * @param id the user UUID.
     * @param request the update payload.
     * @return the updated UserResponseDTO.
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateDTO request
    ) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    /**
     * Toggles the active status of a user account.
     * 
     * @param id the user UUID.
     * @return 204 No Content.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> toggleUserStatus(@PathVariable UUID id) {
        userService.toggleActiveStatus(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Unlocks a user account that was blocked by brute force protection.
     * Resets both the locked flag and the failed attempts counter.
     * 
     * @param id the user UUID.
     * @return 204 No Content.
     */
    @PatchMapping("/{id}/unlock")
    public ResponseEntity<Void> unlockUser(@PathVariable UUID id) {
        userService.unlockAccount(id);
        return ResponseEntity.noContent().build();
    }
}