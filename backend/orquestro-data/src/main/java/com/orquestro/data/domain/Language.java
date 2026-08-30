package com.orquestro.data.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a language supported by the Orquestro platform.
 * This entity is the foundation for the dynamic multi-language system,
 * allowing the application to resolve translations and locale-specific 
 * formatting based on the user's preference.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@Entity
@Table(name = "languages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Language extends BaseEntity {

    /**
     * The full name of the language (e.g., "English", "Português").
     */
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    /**
     * ISO 639-1 language code, optionally followed by an ISO 3166-1 country code.
     * Examples: "en", "pt-BR", "es".
     * This code is used by the system to resolve Locales.
     */
    @Column(name = "code", nullable = false, unique = true, length = 10)
    private String code;

    /**
     * Flag indicating if the language is currently available for users to select.
     */
    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Flag indicating if this is the fallback language for the system.
     * Only one language should be marked as default.
     */
    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;
}