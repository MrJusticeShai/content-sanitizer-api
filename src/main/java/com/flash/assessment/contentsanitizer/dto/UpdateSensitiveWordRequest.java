package com.flash.assessment.contentsanitizer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request payload used to update a sensitive word")
public class UpdateSensitiveWordRequest {

    @NotBlank
    @Schema(description = "The current word to be updated", required = true, example = "SAVED")
    private String currentWord;

    @NotBlank
    @Schema(description = "The new value of the sensitive word", required = true, example = "STORED")
    private String newWord;
}
