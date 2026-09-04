package com.orquestro.data.repository;

import com.orquestro.data.domain.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Language entity operations.
 * Manages supported languages for the platform's internationalization and locale resolution.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@Repository
public interface LanguageRepository extends JpaRepository<Language, UUID> {

    /**
     * Finds a language by its ISO code (e.g., 'en', 'pt-BR').
     * 
     * @param code the language code to search for.
     * @return an Optional containing the found language.
     */
    Optional<Language> findByCode(String code);

    /**
     * Finds the default system language.
     * Only one language should be marked as default in the system.
     * 
     * @return an Optional containing the default language.
     */
    Optional<Language> findByIsDefaultTrue();

    /**
     * Retrieves all languages that are currently marked as active.
     * This is used to populate language selectors in the frontend.
     * 
     * @return a list of active languages.
     */
    List<Language> findAllByActiveTrue();

    /**
     * Checks if another language with the given name exists (case-insensitive), excluding a specific ID.
     */
    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    /**
     * Checks if another language with the given code exists (case-insensitive), excluding a specific ID.
     */
    boolean existsByCodeIgnoreCaseAndIdNot(String code, UUID id);

    /**
     * Checks if a language with the given name exists (case-insensitive).
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Checks if a language with the given code exists (case-insensitive).
     */
    boolean existsByCodeIgnoreCase(String code);
}