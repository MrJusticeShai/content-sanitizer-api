package com.flash.assessment.contentsanitizer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * Response DTO for sensitive words (internal use).
 *
 * <p>
 * This DTO exposes the actual sensitive word because it is intended for internal
 * administration of the sensitive words database. External clients should only
 * interact with the `sanitizeMessage` endpoint, which masks sensitive words.
 * </p>
 */
@Data
@Builder
@Schema(description = "Response payload containing sensitive word information (internal use only)")
public class SensitiveWordResponse {

    @NonNull
    @Schema(description = "Unique identifier for sensitive word")
    private Long id;

    @Schema(description = "The sensitive word itself", example = "password")
    private String word;
}