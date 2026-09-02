package com.orquestro.management.service;

import com.orquestro.data.domain.Language;
import com.orquestro.data.domain.User;
import com.orquestro.data.repository.LanguageRepository;
import com.orquestro.data.repository.UserRepository;
import com.orquestro.management.dto.request.UserUpdateDTO;
import com.orquestro.management.dto.response.UserResponseDTO;
import com.orquestro.management.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service responsible for managing administrative operations related to users.
 * Handles user retrieval with pagination, profile updates, and account status management.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final LanguageRepository languageRepository;

    /**
     * Retrieves a paginated list of all users in the system.
     * 
     * @param pageable the pagination information (page number, size, sorting).
     * @return a page of UserResponseDTOs.
     */
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> findAll(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    /**
     * Finds a specific user by its unique identifier.
     * 
     * @param id the UUID of the user.
     * @return the found user as a DTO.
     * @throws BusinessException if the user is not found.
     */
    @Transactional(readOnly = true)
    public UserResponseDTO findById(UUID id) {
        return userRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new BusinessException("User not found with the provided ID.", HttpStatus.NOT_FOUND));
    }

    /**
     * Updates an existing user's information.
     * Validates email uniqueness before committing changes.
     * 
     * @param id the UUID of the user to be updated.
     * @param request the update data.
     * @return the updated user as a DTO.
     */
    @Transactional
    public UserResponseDTO update(UUID id, UserUpdateDTO request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found to update.", HttpStatus.NOT_FOUND));

        /* Check if new email is already taken by another user */
        if (!user.getEmail().equalsIgnoreCase(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new BusinessException("The new email address is already in use by another account.", HttpStatus.CONFLICT);
        }

        Language language = languageRepository.findById(request.languageId())
                .orElseThrow(() -> new BusinessException("The selected language was not found.", HttpStatus.BAD_REQUEST));

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setGlobalRole(request.globalRole());
        user.setLanguage(language);

        return mapToResponse(userRepository.save(user));
    }

    /**
     * Toggles the active status of a user account.
     * Provides a safe way to disable access without deleting data.
     * 
     * @param id the UUID of the user.
     */
    @Transactional
    public void toggleActiveStatus(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found to change status.", HttpStatus.NOT_FOUND));
        
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    /**
     * Maps a User entity to a detailed UserResponseDTO.
     */
    private UserResponseDTO mapToResponse(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .globalRole(user.getGlobalRole().name())
                .active(user.isActive())
                .accountLocked(user.isAccountLocked())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .language(new UserResponseDTO.LanguageSummaryDTO(
                        user.getLanguage().getId(),
                        user.getLanguage().getName(),
                        user.getLanguage().getCode()
                ))
                .build();
    }
    
    /**
     * Unlocks a user account that was previously locked due to brute force protection.
     * Resets both the locked flag and the failed attempts counter.
     * 
     * @param id the unique identifier of the user to unlock.
     * @throws BusinessException if the user is not found.
     */
    @Transactional
    public void unlockAccount(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found to unlock.", HttpStatus.NOT_FOUND));
        
        user.setAccountLocked(false);
        user.resetFailedAttempts();
        
        userRepository.save(user);
    }
}