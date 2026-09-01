package com.orquestro.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object representing a request to create or update a language.
 * Contains the necessary attributes to define system localization support.
 * 
 * @param name The descriptive name of the language (e.g., "English").
 * @param code The ISO language code (e.g., "en", "pt-BR").
 * @param active Indicates if the language should be available for selection.
 * @param isDefault Indicates if this language should be the system fallback.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
public record LanguageRequestDTO(

    @NotBlank(message = "Language name is required")
    @Size(max = 50, message = "Language name must not exceed 50 characters")
    String name,

    @NotBlank(message = "Language code is required")
    @Size(min = 2, max = 10, message = "Language code must be between 2 and 10 characters")
    String code,

    @NotNull(message = "Active status is required")
    Boolean active,

    @NotNull(message = "Default status is required")
    Boolean isDefault
) {
}