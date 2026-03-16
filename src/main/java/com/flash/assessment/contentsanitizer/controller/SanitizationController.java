package com.flash.assessment.contentsanitizer.controller;

import com.flash.assessment.contentsanitizer.dto.SanitizeResponse;
import com.flash.assessment.contentsanitizer.dto.SantizeRequest;
import com.flash.assessment.contentsanitizer.service.message.SanitizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for sanitizing messages.
 * <p>
 * Accepts a message via DTO and returns a sanitized response DTO.
 * </p>
 */
@RestController
@RequestMapping("/api/sanitize")
@RequiredArgsConstructor
@Tag(name = "Sanitization", description = "Mask sensitive words in messages")
public class SanitizationController {

    private final SanitizationService sanitizationService;

    @PostMapping
    @Operation(summary = "Sanitize a message")
    public ResponseEntity<SanitizeResponse> sanitizeMessage(@Valid @RequestBody SantizeRequest request) {
        String sanitized = sanitizationService.sanitizeMessage(request.getMessage());
        return ResponseEntity.ok(new SanitizeResponse(sanitized));
    }
}