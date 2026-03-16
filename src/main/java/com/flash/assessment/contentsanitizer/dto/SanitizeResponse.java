package com.flash.assessment.contentsanitizer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Data Transfer Object representing the response of the content sanitization process.
 *
 * <p>
 * Contains the sanitized version of the input message after sensitive words
 * have been removed or masked.
 * </p>
 */
@Data
@AllArgsConstructor
public class SanitizeResponse {

    /**
     * The message after sensitive words have been sanitized.
     */
    private String sanitizedMessage;
}
