package com.orquestro.controller;

import com.orquestro.management.dto.request.UserRoleRequestDTO;
import com.orquestro.management.dto.response.UserRoleResponseDTO;
import com.orquestro.management.service.UserRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing high-level user access profiles.
 * Provides endpoints for administrators to define roles and map them 
 * to granular module permissions.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@RestController
@RequestMapping("/user-roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class UserRoleController {

    private final UserRoleService userRoleService;

    /**
     * Retrieves all user role profiles registered in the platform.
     * 
     * @return a ResponseEntity containing the list of all profiles.
     */
    @GetMapping
    public ResponseEntity<List<UserRoleResponseDTO>> getAll() {
        return ResponseEntity.ok(userRoleService.findAll());
    }

    /**
     * Retrieves the details of a specific profile, including its mapped module roles.
     * 
     * @param id the unique identifier of the profile.
     * @return a ResponseEntity containing the profile details.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserRoleResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(userRoleService.findById(id));
    }

    /**
     * Creates a new access profile with initial module role mappings.
     * 
     * @param request the profile data and associations.
     * @return a ResponseEntity with the created profile and 201 Created status.
     */
    @PostMapping
    public ResponseEntity<UserRoleResponseDTO> create(
            @Valid @RequestBody UserRoleRequestDTO request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userRoleService.create(request));
    }

    /**
     * Updates an existing profile's information and synchronizes its mappings.
     * 
     * @param id the unique identifier of the profile to update.
     * @param request the updated data.
     * @return a ResponseEntity containing the updated profile.
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserRoleResponseDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody UserRoleRequestDTO request
    ) {
        return ResponseEntity.ok(userRoleService.update(id, request));
    }

    /**
     * Toggles the active status of a profile.
     * 
     * @param id the unique identifier of the profile.
     * @return 204 No Content status.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> toggleStatus(@PathVariable UUID id) {
        userRoleService.toggleStatus(id);
        return ResponseEntity.noContent().build();
    }
}