package com.orquestro.management.service;

import com.orquestro.data.domain.ModuleRole;
import com.orquestro.data.repository.ModuleRoleRepository;
import com.orquestro.management.dto.request.ModuleRoleRequestDTO;
import com.orquestro.management.dto.response.ModuleRoleResponseDTO;
import com.orquestro.management.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service responsible for managing module-specific roles.
 * Provides administrative operations to define and maintain functional roles 
 * within the application's business domain.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@Service
@RequiredArgsConstructor
public class ModuleRoleService {

    private final ModuleRoleRepository moduleRoleRepository;

    /**
     * Retrieves all module roles registered in the system.
     * 
     * @return a list of all ModuleRoleResponseDTOs.
     */
    @Transactional(readOnly = true)
    public List<ModuleRoleResponseDTO> findAll() {
        return moduleRoleRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Retrieves only the roles that are currently active.
     * Useful for populating selection components in the UI.
     * 
     * @return a list of active ModuleRoleResponseDTOs.
     */
    @Transactional(readOnly = true)
    public List<ModuleRoleResponseDTO> findAllActive() {
        return moduleRoleRepository.findAllByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Creates a new module role after validating that the name is unique.
     * 
     * @param request the role data to be created.
     * @return the created role as a DTO.
     * @throws BusinessException if a role with the same name already exists.
     */
    @Transactional
    public ModuleRoleResponseDTO create(ModuleRoleRequestDTO request) {
        if (moduleRoleRepository.existsByName(request.name())) {
            throw new BusinessException("A module role with this name already exists.", HttpStatus.CONFLICT);
        }

        ModuleRole role = ModuleRole.builder()
                .name(request.name().toUpperCase().trim())
                .description(request.description())
                .active(request.active())
                .build();

        return mapToResponse(moduleRoleRepository.save(role));
    }

    /**
     * Updates an existing module role's information.
     * 
     * @param id the unique identifier of the role.
     * @param request the updated data.
     * @return the updated role as a DTO.
     */
    @Transactional
    public ModuleRoleResponseDTO update(UUID id, ModuleRoleRequestDTO request) {
        ModuleRole role = moduleRoleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Module role not found.", HttpStatus.NOT_FOUND));

        /* Check for name conflict if the name is being changed */
        if (!role.getName().equalsIgnoreCase(request.name()) && moduleRoleRepository.existsByName(request.name())) {
            throw new BusinessException("Another role with this name already exists.", HttpStatus.CONFLICT);
        }

        role.setName(request.name().toUpperCase().trim());
        role.setDescription(request.description());
        role.setActive(request.active());

        return mapToResponse(moduleRoleRepository.save(role));
    }

    /**
     * Toggles the active status of a module role.
     * 
     * @param id the unique identifier of the role.
     */
    @Transactional
    public void toggleStatus(UUID id) {
        ModuleRole role = moduleRoleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Module role not found to update status.", HttpStatus.NOT_FOUND));
        
        role.setActive(!role.isActive());
        moduleRoleRepository.save(role);
    }

    /**
     * Maps a ModuleRole entity to its response DTO.
     */
    private ModuleRoleResponseDTO mapToResponse(ModuleRole role) {
        return new ModuleRoleResponseDTO(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.isActive()
        );
    }
}