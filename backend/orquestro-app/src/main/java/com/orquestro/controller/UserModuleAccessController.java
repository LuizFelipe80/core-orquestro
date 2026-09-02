package com.orquestro.controller;

import com.orquestro.management.dto.request.UserModuleAccessRequestDTO;
import com.orquestro.management.dto.response.UserModuleAccessResponseDTO;
import com.orquestro.management.service.UserModuleAccessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing granular user access grants within the module.
 * Provides endpoints for administrators to assign and revoke functional roles
 * to specific users, enabling fine-grained security control.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@RestController
@RequestMapping("/user-access")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class UserModuleAccessController {

    private final UserModuleAccessService accessService;

    /**
     * Retrieves all user access assignments in the current module.
     * 
     * @return a list of all UserModuleAccessResponseDTOs.
     */
    @GetMapping
    public ResponseEntity<List<UserModuleAccessResponseDTO>> getAll() {
        return ResponseEntity.ok(accessService.findAll());
    }

    /**
     * Retrieves all access grants assigned to a specific user.
     * 
     * @param userId the unique identifier of the user.
     * @return a list of access grants for the given user.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserModuleAccessResponseDTO>> getByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(accessService.findByUserId(userId));
    }

    /**
     * Grants a module-specific role to a user.
     * 
     * @param request the grant details (user ID and role ID).
     * @return the created access record with 201 Created status.
     */
    @PostMapping
    public ResponseEntity<UserModuleAccessResponseDTO> grantAccess(
            @Valid @RequestBody UserModuleAccessRequestDTO request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accessService.grantAccess(request));
    }

    /**
     * Toggles the active status of an existing access grant.
     * 
     * @param id the unique identifier of the access record.
     * @return 204 No Content status.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> toggleStatus(@PathVariable UUID id) {
        accessService.toggleStatus(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Permanently revokes (removes) an access grant.
     * 
     * @param id the unique identifier of the access record to be deleted.
     * @return 204 No Content status.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revokeAccess(@PathVariable UUID id) {
        accessService.revokeAccess(id);
        return ResponseEntity.noContent().build();
    }
}