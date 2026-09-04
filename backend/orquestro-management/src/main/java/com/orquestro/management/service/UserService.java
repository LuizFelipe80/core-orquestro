package com.orquestro.management.service;

import com.orquestro.data.domain.Language;
import com.orquestro.data.domain.User;
import com.orquestro.data.domain.UserRole;
import com.orquestro.data.repository.LanguageRepository;
import com.orquestro.data.repository.UserRepository;
import com.orquestro.data.repository.UserRoleRepository;
import com.orquestro.management.dto.request.PasswordChangeRequestDTO;
import com.orquestro.management.dto.request.UserCreateRequestDTO;
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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service responsible for managing administrative and self-service user operations.
 * Implements a flexible RBAC model that allows users to hold multiple access profiles
 * simultaneously, enabling permission aggregation across different modules.
 * Includes privilege escalation protection to ensure only ADMINISTRATORs can grant ADMINISTRATOR roles.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private static final String DEFAULT_ROLE = "USER";
    private static final String ADMIN_ROLE = "ADMINISTRATOR";

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final LanguageRepository languageRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Retrieves a paginated list of all users.
     */
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> findAll(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    /**
     * Finds a specific user by its unique identifier.
     */
    @Transactional(readOnly = true)
    public UserResponseDTO findById(UUID id) {
        return userRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new BusinessException("User not found with the provided ID.", HttpStatus.NOT_FOUND));
    }

    /**
     * Administratively creates a new user with configured roles and language.
     * Enforces privilege escalation protection.
     * 
     * @param request the user creation data.
     * @return the created UserResponseDTO.
     */
    @Transactional
    public UserResponseDTO create(UserCreateRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email address already in use.", HttpStatus.CONFLICT);
        }

        Language language = languageRepository.findById(request.languageId())
                .orElseThrow(() -> new BusinessException("The selected language was not found.", HttpStatus.BAD_REQUEST));

        Set<String> roleNames = (request.roles() != null && !request.roles().isEmpty()) 
                ? request.roles() 
                : Set.of(DEFAULT_ROLE);

        // Security check: only an ADMINISTRATOR can assign the ADMINISTRATOR role
        if (roleNames.stream().anyMatch(r -> r.equalsIgnoreCase(ADMIN_ROLE)) && !isCallerAdmin()) {
            throw new BusinessException("Only an ADMINISTRATOR can assign the ADMINISTRATOR role.", HttpStatus.FORBIDDEN);
        }

        Set<UserRole> targetRoles = new HashSet<>();
        for (String roleName : roleNames) {
            UserRole role = userRoleRepository.findByName(roleName)
                    .orElseThrow(() -> new BusinessException("Role not found: " + roleName, HttpStatus.BAD_REQUEST));
            targetRoles.add(role);
        }

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .language(language)
                .active(true)
                .accountLocked(false)
                .roles(targetRoles)
                .build();

        return mapToResponse(userRepository.save(user));
    }

    /**
     * Updates an existing user's information and access roles.
     * Enforces privilege escalation protection.
     * 
     * @param id the user UUID.
     * @param request the update data containing a set of role names.
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

        /* Resolves the set of UserRole entities based on the names provided in the DTO */
        Set<UserRole> targetRoles = request.roles().stream()
                .map(roleName -> userRoleRepository.findByName(roleName)
                        .orElseThrow(() -> new BusinessException("Role not found: " + roleName, HttpStatus.BAD_REQUEST)))
                .collect(Collectors.toSet());

        // Privilege Escalation Protection: Only an ADMINISTRATOR can modify an existing ADMINISTRATOR or grant the ADMINISTRATOR role
        boolean existingIsAdmin = user.getRoles().stream().anyMatch(r -> r.getName().equalsIgnoreCase(ADMIN_ROLE));
        boolean targetIsAdmin = targetRoles.stream().anyMatch(r -> r.getName().equalsIgnoreCase(ADMIN_ROLE));
        if ((existingIsAdmin || targetIsAdmin) && !isCallerAdmin()) {
            throw new BusinessException("Only an ADMINISTRATOR can modify an ADMINISTRATOR user or assign the ADMINISTRATOR role.", HttpStatus.FORBIDDEN);
        }

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setLanguage(language);
        user.getRoles().clear();
        user.getRoles().addAll(targetRoles);

        return mapToResponse(userRepository.save(user));
    }

    /**
     * Verifies if the currently authenticated principal holds the ROLE_ADMINISTRATOR authority.
     */
    private boolean isCallerAdmin() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + ADMIN_ROLE) || a.getAuthority().equals(ADMIN_ROLE));
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
     * Internal helper to map User entity to UserResponseDTO.
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