package com.orquestro.management.service;

import com.orquestro.data.domain.Language;
import com.orquestro.data.repository.LanguageRepository;
import com.orquestro.management.dto.request.LanguageRequestDTO;
import com.orquestro.management.dto.response.LanguageResponseDTO;
import com.orquestro.management.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service responsible for managing business logic related to system languages.
 * Handles the lifecycle of localization settings, ensuring that only one
 * language is set as the system default at any given time.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@Service
@RequiredArgsConstructor
public class LanguageService {

    private final LanguageRepository languageRepository;

    /**
     * Retrieves all languages registered in the system, regardless of status.
     * Used for administrative management.
     * 
     * @return A list of all LanguageResponseDTOs.
     */
    @Transactional(readOnly = true)
    public List<LanguageResponseDTO> findAll() {
        return languageRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Retrieves all active languages available in the system.
     * 
     * @return A list of active LanguageResponseDTOs.
     */
    @Transactional(readOnly = true)
    public List<LanguageResponseDTO> getAllActiveLanguages() {
        return languageRepository.findAllByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Finds a specific language by its ID.
     * 
     * @param id The UUID of the language.
     * @return The LanguageResponseDTO.
     */
    @Transactional(readOnly = true)
    public LanguageResponseDTO findById(UUID id) {
        return languageRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new BusinessException("Language not found.", HttpStatus.NOT_FOUND));
    }

    /**
     * Creates a new language in the system.
     * Ensures uniqueness of name and code, and manages the default flag.
     * 
     * @param request The language data to create.
     * @return The created LanguageResponseDTO.
     */
    @Transactional
    public LanguageResponseDTO create(LanguageRequestDTO request) {
        validateUniqueness(null, request.name(), request.code());

        if (Boolean.TRUE.equals(request.isDefault())) {
            handleDefaultLanguageSwitch();
        }

        Language language = Language.builder()
                .name(request.name())
                .code(request.code())
                .active(request.active())
                .isDefault(request.isDefault())
                .build();

        return mapToResponse(languageRepository.save(language));
    }

    /**
     * Updates an existing language.
     * 
     * @param id The ID of the language to update.
     * @param request The new data.
     * @return The updated LanguageResponseDTO.
     */
    @Transactional
    public LanguageResponseDTO update(UUID id, LanguageRequestDTO request) {
        Language language = languageRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Language not found to update.", HttpStatus.NOT_FOUND));

        validateUniqueness(id, request.name(), request.code());

        if (Boolean.TRUE.equals(request.isDefault()) && !Boolean.TRUE.equals(language.getIsDefault())) {
            handleDefaultLanguageSwitch();
        }

        language.setName(request.name());
        language.setCode(request.code());
        language.setActive(request.active());
        language.setIsDefault(request.isDefault());

        return mapToResponse(languageRepository.save(language));
    }

    /**
     * Deletes a language from the system.
     * Prevents deletion of the default system language.
     * 
     * @param id The ID of the language to delete.
     */
    @Transactional
    public void delete(UUID id) {
        Language language = languageRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Language not found to delete.", HttpStatus.NOT_FOUND));

        if (Boolean.TRUE.equals(language.getIsDefault())) {
            throw new BusinessException("The default system language cannot be deleted.", HttpStatus.BAD_REQUEST);
        }

        languageRepository.delete(language);
    }

    /**
     * Ensures that only one language is marked as default.
     * If a new default is set, the previous one is updated to false.
     */
    private void handleDefaultLanguageSwitch() {
        languageRepository.findByIsDefaultTrue()
                .ifPresent(oldDefault -> {
                    oldDefault.setIsDefault(false);
                    languageRepository.save(oldDefault);
                });
    }

    /**
     * Validates that the name and code are not already used by another language.
     */
    private void validateUniqueness(UUID id, String name, String code) {
        languageRepository.findAll().stream()
                .filter(l -> !l.getId().equals(id))
                .forEach(l -> {
                    if (l.getName().equalsIgnoreCase(name)) {
                        throw new BusinessException("A language with this name already exists.", HttpStatus.CONFLICT);
                    }
                    if (l.getCode().equalsIgnoreCase(code)) {
                        throw new BusinessException("A language with this code already exists.", HttpStatus.CONFLICT);
                    }
                });
    }

    /**
     * Maps a Language entity to a LanguageResponseDTO.
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