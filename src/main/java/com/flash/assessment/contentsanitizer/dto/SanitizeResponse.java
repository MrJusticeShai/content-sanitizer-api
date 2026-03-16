package com.flash.assessment.contentsanitizer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Response payload containing the sanitized message")
public class SanitizeResponse {

    @Schema(description = "The sanitized message", example = "My ****** is 12345")
    private String sanitizedMessage;
}
