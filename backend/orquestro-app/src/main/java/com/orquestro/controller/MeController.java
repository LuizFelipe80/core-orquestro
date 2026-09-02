package com.orquestro.controller;

import com.orquestro.management.dto.request.PasswordChangeRequestDTO;
import com.orquestro.management.dto.request.UserProfileUpdateDTO;
import com.orquestro.management.dto.response.UserResponseDTO;
import com.orquestro.management.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for self-service user operations.
 * Allows the currently authenticated user to manage their own profile 
 * and security settings without requiring administrative privileges.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class MeController {

    private final UserService userService;

    /**
     * Retrieves the profile details of the currently authenticated user.
     * 
     * @return a ResponseEntity containing the user's profile data.
     */
    @GetMapping
    public ResponseEntity<UserResponseDTO> getMyProfile() {
        return ResponseEntity.ok(userService.getCurrentUserProfile());
    }

    /**
     * Updates the names and language preference of the currently authenticated user.
     * 
     * @param request the profile update data.
     * @return a ResponseEntity containing the updated profile data.
     */
    @PutMapping
    public ResponseEntity<UserResponseDTO> updateMyProfile(
            @Valid @RequestBody UserProfileUpdateDTO request
    ) {
        return ResponseEntity.ok(userService.updateCurrentUserProfile(request));
    }

    /**
     * Updates the password for the currently authenticated user.
     * 
     * @param request the password change data including current and new password.
     * @return a 204 No Content response on success.
     */
    @PatchMapping("/password")
    public ResponseEntity<Void> changeMyPassword(
            @Valid @RequestBody PasswordChangeRequestDTO request
    ) {
        userService.changePassword(request);
        return ResponseEntity.noContent().build();
    }
}