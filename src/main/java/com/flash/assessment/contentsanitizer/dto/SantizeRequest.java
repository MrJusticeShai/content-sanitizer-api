package com.flash.assessment.contentsanitizer.dto;

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
public class SantizeRequest {

    /**
     * The message to be sanitized.
     * Must not be blank.
     */
    @NotBlank
    private String message;
}
