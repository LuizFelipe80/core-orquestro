package com.orquestro.management.service;

import com.orquestro.data.domain.ModuleRole;
import com.orquestro.data.domain.UserRole;
import com.orquestro.data.repository.ModuleRoleRepository;
import com.orquestro.data.repository.UserRoleRepository;
import com.orquestro.management.dto.request.UserRoleRequestDTO;
import com.orquestro.management.dto.response.UserRoleResponseDTO;
import com.orquestro.management.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service responsible for managing high-level user access profiles.
 * It handles the mapping between profiles (UserRoles) and granular 
 * permissions (ModuleRoles), enabling an indirect RBAC strategy.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;
    private final ModuleRoleRepository moduleRoleRepository;

    /**
     * Retrieves all user role profiles registered in the system.
     * 
     * @return a list of all UserRoleResponseDTOs.
     */
    @Transactional(readOnly = true)
    public List<UserRoleResponseDTO> findAll() {
        return userRoleRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Finds a specific user role profile by its identifier.
     * 
     * @param id the UUID of the user role.
     * @return the detailed UserRoleResponseDTO.
     */
    @Transactional(readOnly = true)
    public UserRoleResponseDTO findById(UUID id) {
        return userRoleRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new BusinessException("User role profile not found.", HttpStatus.NOT_FOUND));
    }

    /**
     * Creates a new access profile and maps it to the specified module roles.
     * 
     * @param request the profile data and role associations.
     * @return the created profile as a DTO.
     */
    @Transactional
    public UserRoleResponseDTO create(UserRoleRequestDTO request) {
        if (userRoleRepository.existsByName(request.name())) {
            throw new BusinessException("A profile with this name already exists.", HttpStatus.CONFLICT);
        }

        Set<ModuleRole> moduleRoles = new HashSet<>();
        if (request.moduleRoleIds() != null && !request.moduleRoleIds().isEmpty()) {
            moduleRoles.addAll(moduleRoleRepository.findAllById(request.moduleRoleIds()));
        }

        UserRole userRole = UserRole.builder()
                .name(request.name().toUpperCase().trim())
                .description(request.description())
                .active(request.active())
                .moduleRoles(moduleRoles)
                .build();

        return mapToResponse(userRoleRepository.save(userRole));
    }

    /**
     * Updates an existing profile and synchronizes its granular permissions.
     * 
     * @param id the unique identifier of the profile to update.
     * @param request the new profile data and role list.
     * @return the updated profile as a DTO.
     */
    @Transactional
    public UserRoleResponseDTO update(UUID id, UserRoleRequestDTO request) {
        UserRole userRole = userRoleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Profile not found to update.", HttpStatus.NOT_FOUND));

        if (!userRole.getName().equalsIgnoreCase(request.name()) && userRoleRepository.existsByName(request.name())) {
            throw new BusinessException("Another profile with this name already exists.", HttpStatus.CONFLICT);
        }

        userRole.setName(request.name().toUpperCase().trim());
        userRole.setDescription(request.description());
        userRole.setActive(request.active());

        /* Synchronize module roles mapping */
        userRole.getModuleRoles().clear();
        if (request.moduleRoleIds() != null && !request.moduleRoleIds().isEmpty()) {
            userRole.getModuleRoles().addAll(moduleRoleRepository.findAllById(request.moduleRoleIds()));
        }

        return mapToResponse(userRoleRepository.save(userRole));
    }

    /**
     * Toggles the active status of an access profile.
     * 
     * @param id the UUID of the profile.
     */
    @Transactional
    public void toggleStatus(UUID id) {
        UserRole userRole = userRoleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Profile not found to change status.", HttpStatus.NOT_FOUND));
        
        userRole.setActive(!userRole.isActive());
        userRoleRepository.save(userRole);
    }

    /**
     * Internal helper to map the UserRole entity to its response DTO.
     */
    private UserRoleResponseDTO mapToResponse(UserRole role) {
        Set<UserRoleResponseDTO.ModuleRoleSummaryDTO> moduleRoleSummaries = role.getModuleRoles().stream()
                .map(mr -> new UserRoleResponseDTO.ModuleRoleSummaryDTO(mr.getId(), mr.getName()))
                .collect(Collectors.toSet());

        return new UserRoleResponseDTO(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.isActive(),
                moduleRoleSummaries
        );
    }
}