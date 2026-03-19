package com.flash.assessment.contentsanitizer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Data Transfer Object for submitting a message to be sanitized.
 *
 * <p>
 * This DTO captures the input message that needs processing to remove
 * or mask sensitive words.
 * </p>
 *
 * <p>
 * Validation:
 * <ul>
 *     <li>{@link #message} must not be blank.</li>
 * </ul>
 * </p>
 */
@Data
@Schema(description = "Request payload containing the message to be sanitized")
public class SanitizeRequest {

    @NotBlank
    @Schema(description = "The message that needs sanitization", required = true, example = "My drop is 12345")
    private String message;
}
