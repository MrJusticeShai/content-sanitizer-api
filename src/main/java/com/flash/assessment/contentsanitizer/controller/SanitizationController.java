package com.flash.assessment.contentsanitizer.controller;

import com.flash.assessment.contentsanitizer.dto.SanitizeResponse;
import com.flash.assessment.contentsanitizer.dto.SanitizeRequest;
import com.flash.assessment.contentsanitizer.service.message.SanitizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

/**
 * REST controller for sanitizing messages (external client use).
 * <p>
 * Accepts a message via DTO and returns a sanitized response DTO.
 * Internal sensitive words are never exposed.
 * </p>
 */
@RestController
@RequestMapping("/api/sanitize")
@RequiredArgsConstructor
@Tag(name = "Sanitization", description = "Mask sensitive words in messages")
public class SanitizationController {

    private final SanitizationService sanitizationService;

    /**
     * Sanitizes the provided message by masking all sensitive words.
     *
     * @param request {@link SanitizeRequest} containing the message to sanitize
     * @return sanitized message in {@link SanitizeResponse}
     */
    @PostMapping
    @Operation(summary = "Sanitize a message", description = "Masks all sensitive words in the provided message and returns the sanitized result.")
    @ApiResponse(responseCode = "200", description = "Sanitized message returned")
    public ResponseEntity<SanitizeResponse> sanitizeMessage(@Valid @RequestBody SanitizeRequest request) {
        String sanitized = sanitizationService.sanitizeMessage(request.getMessage());
        return ResponseEntity.ok(new SanitizeResponse(sanitized));
    }
}