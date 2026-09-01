package com.orquestro.controller;

import com.orquestro.management.dto.request.LanguageRequestDTO;
import com.orquestro.management.dto.response.LanguageResponseDTO;
import com.orquestro.management.service.LanguageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing system languages and localization settings.
 * Provides endpoints for administrative CRUD operations and public language discovery.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@RestController
@RequestMapping("/languages")
@RequiredArgsConstructor
public class LanguageController {

    private final LanguageService languageService;

    /**
     * Retrieves all languages registered in the system.
     * Restricted to administrative roles.
     * 
     * @return a list of all languages.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<LanguageResponseDTO>> getAllLanguages() {
        return ResponseEntity.ok(languageService.findAll());
    }

    /**
     * Retrieves only the languages marked as active.
     * Accessible to any authenticated user to allow for profile language selection.
     * 
     * @return a list of active languages.
     */
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LanguageResponseDTO>> getActiveLanguages() {
        return ResponseEntity.ok(languageService.getAllActiveLanguages());
    }

    /**
     * Retrieves a specific language by its unique identifier.
     * 
     * @param id the UUID of the language.
     * @return the language details.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<LanguageResponseDTO> getLanguageById(@PathVariable UUID id) {
        return ResponseEntity.ok(languageService.findById(id));
    }

    /**
     * Creates a new language in the platform.
     * Restricted to ADMIN role only.
     * 
     * @param request the language data.
     * @return the created language with 201 Created status.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LanguageResponseDTO> createLanguage(
            @Valid @RequestBody LanguageRequestDTO request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(languageService.create(request));
    }

    /**
     * Updates an existing language's configuration.
     * Restricted to ADMIN role only.
     * 
     * @param id the UUID of the language to update.
     * @param request the new language data.
     * @return the updated language details.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LanguageResponseDTO> updateLanguage(
            @PathVariable UUID id,
            @Valid @RequestBody LanguageRequestDTO request
    ) {
        return ResponseEntity.ok(languageService.update(id, request));
    }

    /**
     * Permanently removes a language from the system.
     * Restricted to ADMIN role only.
     * 
     * @param id the UUID of the language to delete.
     * @return a 204 No Content response.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLanguage(@PathVariable UUID id) {
        languageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}