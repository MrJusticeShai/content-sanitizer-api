package com.flash.assessment.contentsanitizer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * Response DTO for sensitive words (external API).
 *
 * <p>
 * Contains only a masked version of the word to avoid leaking sensitive content.
 * </p>
 */
@Data
@Builder
@Schema(description = "Response payload containing sensitive word information")
public class SensitiveWordResponse {

    @NonNull
    @Schema(description = "Unique identifier of sensitive word")
    private Long id;

    @Schema(description = "The sensitive word itself", example = "password")
    private String wordMasked;
}