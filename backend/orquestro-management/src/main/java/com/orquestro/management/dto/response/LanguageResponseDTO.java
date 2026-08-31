package com.orquestro.management.dto.response;

import java.util.UUID;

/**
 * Data Transfer Object representing a language response.
 * Utilizes Java Records for immutability and concise data representation.
 * This DTO is used to send language information to the client layer.
 * 
 * @param id The unique identifier of the language.
 * @param name The full name of the language (e.g., English).
 * @param code The ISO language code (e.g., en).
 * @param isDefault Indicates if this is the system's primary language.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
public record LanguageResponseDTO(
    UUID id,
    String name,
    String code,
    boolean isDefault
) {
}