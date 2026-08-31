package com.orquestro.management.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.orquestro.data.domain.Language;
import com.orquestro.data.repository.LanguageRepository;
import com.orquestro.management.dto.response.LanguageResponseDTO;

import lombok.RequiredArgsConstructor;

/**
 * Service class responsible for managing business logic related to system languages.
 * It acts as an intermediary between the data layer and the application layer,
 * handling internationalization settings and default locale resolution.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@Service
@RequiredArgsConstructor
public class LanguageService {

    private final LanguageRepository languageRepository;

    /**
     * Retrieves all active languages available in the system.
     * This is primarily used by the frontend to populate language selectors.
     * 
     * @return A list of LanguageResponseDTO containing active languages.
     */
    @Transactional(readOnly = true)
    public List<LanguageResponseDTO> getAllActiveLanguages() {
        return languageRepository.findAllByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Retrieves the default language configured for the Orquestro platform.
     * Every system installation must have at least one default language.
     * 
     * @return The default LanguageResponseDTO.
     * @throws RuntimeException if no default language is found in the database.
     */
    @Transactional(readOnly = true)
    public LanguageResponseDTO getDefaultLanguage() {
        return languageRepository.findByIsDefaultTrue()
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Default language not found. Please ensure the database is properly seeded."));
    }

    /**
     * Maps a Language entity to a LanguageResponseDTO.
     * 
     * @param language The entity to be converted.
     * @return A new instance of LanguageResponseDTO.
     */
    private LanguageResponseDTO mapToResponse(Language language) {
        return new LanguageResponseDTO(
                language.getId(),
                language.getName(),
                language.getCode(),
                language.getIsDefault()
        );
    }
}