package com.orquestro.controller;

import com.orquestro.management.dto.request.ModuleRoleRequestDTO;
import com.orquestro.management.dto.response.ModuleRoleResponseDTO;
import com.orquestro.management.service.ModuleRoleService;
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
 * REST controller for managing project-specific functional roles.
 * Provides endpoints for administrators to define and maintain roles 
 * relevant to the current business module.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@RestController
@RequestMapping("/module-roles")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class ModuleRoleController {

    private final ModuleRoleService moduleRoleService;

    /**
     * Retrieves all module roles registered in the system.
     * 
     * @return a list of all module roles.
     */
    @GetMapping
    public ResponseEntity<List<ModuleRoleResponseDTO>> getAll() {
        return ResponseEntity.ok(moduleRoleService.findAll());
    }

    /**
     * Retrieves only the roles that are currently active.
     * 
     * @return a list of active module roles.
     */
    @GetMapping("/active")
    public ResponseEntity<List<ModuleRoleResponseDTO>> getActive() {
        return ResponseEntity.ok(moduleRoleService.findAllActive());
    }

    /**
     * Creates a new module-specific role.
     * 
     * @param request the role data.
     * @return the created role with 201 Created status.
     */
    @PostMapping
    public ResponseEntity<ModuleRoleResponseDTO> create(
            @Valid @RequestBody ModuleRoleRequestDTO request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(moduleRoleService.create(request));
    }

    /**
     * Updates an existing module role.
     * 
     * @param id the unique identifier of the role.
     * @param request the new role data.
     * @return the updated role details.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ModuleRoleResponseDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody ModuleRoleRequestDTO request
    ) {
        return ResponseEntity.ok(moduleRoleService.update(id, request));
    }

    /**
     * Toggles the active status of a module role.
     * 
     * @param id the unique identifier of the role.
     * @return 204 No Content status.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> toggleStatus(@PathVariable UUID id) {
        moduleRoleService.toggleStatus(id);
        return ResponseEntity.noContent().build();
    }
}