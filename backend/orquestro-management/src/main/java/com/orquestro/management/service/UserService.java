package com.orquestro.management.service;

import com.orquestro.data.domain.Language;
import com.orquestro.data.domain.User;
import com.orquestro.data.domain.UserRole;
import com.orquestro.data.repository.LanguageRepository;
import com.orquestro.data.repository.UserRepository;
import com.orquestro.data.repository.UserRoleRepository;
import com.orquestro.management.dto.request.PasswordChangeRequestDTO;
import com.orquestro.management.dto.request.UserProfileUpdateDTO;
import com.orquestro.management.dto.request.UserUpdateDTO;
import com.orquestro.management.dto.response.UserResponseDTO;
import com.orquestro.management.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service responsible for managing administrative and self-service user operations.
 * Updated to handle the indirect RBAC model where users possess multiple UserRoles.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final LanguageRepository languageRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Retrieves a paginated list of all users.
     * 
     * @param pageable pagination details.
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
     * @return the user details as a DTO.
     */
    @Transactional(readOnly = true)
    public UserResponseDTO findById(UUID id) {
        return userRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new BusinessException("User not found with the provided ID.", HttpStatus.NOT_FOUND));
    }

    /**
     * Updates an existing user's administrative information.
     * 
     * @param id the user UUID.
     * @param request the update data.
     * @return the updated user details.
     */
    @Transactional
    public UserResponseDTO update(UUID id, UserUpdateDTO request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found to update.", HttpStatus.NOT_FOUND));

        if (!user.getEmail().equalsIgnoreCase(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new BusinessException("The new email address is already in use.", HttpStatus.CONFLICT);
        }

        Language language = languageRepository.findById(request.languageId())
                .orElseThrow(() -> new BusinessException("The selected language was not found.", HttpStatus.BAD_REQUEST));

        /* Logic to update roles: for now, we assume the DTO sends the primary role name */
        UserRole targetRole = userRoleRepository.findByName(request.globalRole())
                .orElseThrow(() -> new BusinessException("The specified role does not exist.", HttpStatus.BAD_REQUEST));

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setLanguage(language);
        user.setRoles(Set.of(targetRole));

        return mapToResponse(userRepository.save(user));
    }

    /**
     * Toggles account activation status.
     */
    @Transactional
    public void toggleActiveStatus(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found.", HttpStatus.NOT_FOUND));
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    /**
     * Unlocks a user account and resets failed attempts.
     */
    @Transactional
    public void unlockAccount(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found to unlock.", HttpStatus.NOT_FOUND));
        user.setAccountLocked(false);
        user.resetFailedAttempts();
        userRepository.save(user);
    }

    /**
     * Self-service: Retrieves the profile of the logged-in user.
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getCurrentUserProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User session not found.", HttpStatus.UNAUTHORIZED));
        return mapToResponse(user);
    }

    /**
     * Self-service: Updates names and language for the logged-in user.
     */
    @Transactional
    public UserResponseDTO updateCurrentUserProfile(UserProfileUpdateDTO request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User session not found.", HttpStatus.UNAUTHORIZED));

        Language language = languageRepository.findById(request.languageId())
                .orElseThrow(() -> new BusinessException("Selected language not found.", HttpStatus.BAD_REQUEST));

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setLanguage(language);

        return mapToResponse(userRepository.save(user));
    }

    /**
     * Self-service: Changes the password for the logged-in user.
     */
    @Transactional
    public void changePassword(PasswordChangeRequestDTO request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User session not found.", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BusinessException("The current password provided is incorrect.", HttpStatus.UNAUTHORIZED);
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    /**
     * Maps a User entity to a UserResponseDTO, extracting role names from the collection.
     */
    private UserResponseDTO mapToResponse(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(UserRole::getName)
                .collect(Collectors.toSet());

        return UserResponseDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .roles(roleNames)
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
}