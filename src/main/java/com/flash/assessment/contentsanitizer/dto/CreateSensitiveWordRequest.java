package com.flash.assessment.contentsanitizer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Data Transfer Object for creating a new {@link com.flash.assessment.contentsanitizer.entity.SensitiveWord}.
 *
 * <p>
 * This DTO is used to capture input data when a client requests
 * the creation of a new sensitive word in the system.
 * </p>
 *
 * <p>
 * Validation:
 * <ul>
 *     <li>{@link #word} must not be blank.</li>
 * </ul>
 * </p>
 */
@Data
@Schema(description = "Request payload used to create a sensitive word")
public class CreateSensitiveWordRequest {

    @NotBlank
    @Schema(description = "The sensitive word to be added", required = true, example = "password")
    private String word;
}
