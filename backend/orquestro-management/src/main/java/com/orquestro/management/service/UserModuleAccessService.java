package com.orquestro.management.service;

import com.orquestro.data.domain.ModuleRole;
import com.orquestro.data.domain.User;
import com.orquestro.data.domain.UserModuleAccess;
import com.orquestro.data.repository.ModuleRoleRepository;
import com.orquestro.data.repository.UserModuleAccessRepository;
import com.orquestro.data.repository.UserRepository;
import com.orquestro.management.dto.request.UserModuleAccessRequestDTO;
import com.orquestro.management.dto.response.UserModuleAccessResponseDTO;
import com.orquestro.management.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service responsible for managing user access grants within the module.
 * Orchestrates the relationship between global identities and business-specific roles,
 * ensuring the integrity of the Granular Role-Based Access Control (RBAC).
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@Service
@RequiredArgsConstructor
public class UserModuleAccessService {

    private final UserModuleAccessRepository accessRepository;
    private final UserRepository userRepository;
    private final ModuleRoleRepository roleRepository;

    /**
     * Retrieves all access grants currently defined in the system.
     * 
     * @return a list of all UserModuleAccessResponseDTOs.
     */
    @Transactional(readOnly = true)
    public List<UserModuleAccessResponseDTO> findAll() {
        return accessRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Retrieves all access grants for a specific user.
     * 
     * @param userId the unique identifier of the user.
     * @return a list of access grants associated with the user.
     */
    @Transactional(readOnly = true)
    public List<UserModuleAccessResponseDTO> findByUserId(UUID userId) {
        return accessRepository.findAllByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Grants a specific module role to a user.
     * Validates both entities existence and prevents duplicate assignments.
     * 
     * @param request the grant details containing user and role IDs.
     * @return the created access record as a DTO.
     * @throws BusinessException if user/role not found or if access already exists.
     */
    @Transactional
    public UserModuleAccessResponseDTO grantAccess(UserModuleAccessRequestDTO request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new BusinessException("User not found to grant access.", HttpStatus.NOT_FOUND));

        ModuleRole role = roleRepository.findById(request.moduleRoleId())
                .orElseThrow(() -> new BusinessException("Module role not found to grant access.", HttpStatus.NOT_FOUND));

        /* Check for existing assignment to prevent unique constraint violation */
        boolean alreadyExists = accessRepository.findAllByUserId(user.getId())
                .stream()
                .anyMatch(access -> access.getModuleRole().getId().equals(role.getId()));

        if (alreadyExists) {
            throw new BusinessException("This user already has this specific role assigned.", HttpStatus.CONFLICT);
        }

        UserModuleAccess access = UserModuleAccess.builder()
                .user(user)
                .moduleRole(role)
                .active(true)
                .build();

        return mapToResponse(accessRepository.save(access));
    }

    /**
     * Toggles the active status of a specific access grant.
     * 
     * @param id the unique identifier of the access record.
     */
    @Transactional
    public void toggleStatus(UUID id) {
        UserModuleAccess access = accessRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Access record not found.", HttpStatus.NOT_FOUND));

        access.setActive(!access.isActive());
        accessRepository.save(access);
    }

    /**
     * Permanently revokes (deletes) an access grant.
     * 
     * @param id the unique identifier of the access record.
     */
    @Transactional
    public void revokeAccess(UUID id) {
        if (!accessRepository.existsById(id)) {
            throw new BusinessException("Access record not found to revoke.", HttpStatus.NOT_FOUND);
        }
        accessRepository.deleteById(id);
    }

    /**
     * Helper method to map UserModuleAccess entity to its response DTO.
     */
    private UserModuleAccessResponseDTO mapToResponse(UserModuleAccess access) {
        return new UserModuleAccessResponseDTO(
                access.getId(),
                access.getUser().getId(),
                access.getUser().getFullName(),
                access.getModuleRole().getId(),
                access.getModuleRole().getName(),
                access.isActive()
        );
    }
}