package com.flash.assessment.contentsanitizer.controller;

import com.flash.assessment.contentsanitizer.dto.CreateSensitiveWordRequest;
import com.flash.assessment.contentsanitizer.dto.SensitiveWordResponse;
import com.flash.assessment.contentsanitizer.dto.UpdateSensitiveWordRequest;
import com.flash.assessment.contentsanitizer.entity.SensitiveWord;
import com.flash.assessment.contentsanitizer.mapper.SensitiveWordMapper;
import com.flash.assessment.contentsanitizer.service.word.SensitiveWordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for internal CRUD operations on sensitive words.
 *
 * <p>
 * This controller is intended for internal administration of the sensitive-words database.
 * The real words are exposed here because these endpoints are NOT publicly accessible.
 * For external clients, only the sanitizeMessage endpoint should be used.
 * </p>
 */
@RestController
@RequestMapping("/api/internal/sensitive-words")
@RequiredArgsConstructor
@Tag(name = "Sensitive Word Management (Internal)", description = "Internal CRUD operations for sensitive words")
public class SensitiveWordController {

    private final SensitiveWordService wordService;
    private final SensitiveWordMapper mapper;

    // ----------------- CREATE -----------------
    @PostMapping
    @Operation(summary = "Create a new sensitive word")
    public ResponseEntity<SensitiveWordResponse> createWord(@Valid @RequestBody CreateSensitiveWordRequest request) {
        SensitiveWord created = wordService.createWord(request);
        return ResponseEntity.ok(mapper.toResponse(created));
    }

    // ----------------- READ ALL -----------------
    @GetMapping
    @Operation(summary = "Get all sensitive words")
    public ResponseEntity<List<SensitiveWordResponse>> getAllWords() {
        List<SensitiveWord> words = wordService.getAllWords();
        List<SensitiveWordResponse> responses = words.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // ----------------- UPDATE -----------------
    @PutMapping
    @Operation(summary = "Update an existing sensitive word (masked)")
    public ResponseEntity<SensitiveWordResponse> updateWord(
            @Valid @RequestBody UpdateSensitiveWordRequest request) {

        SensitiveWord updated = wordService.updateWord(request);
        return ResponseEntity.ok(mapper.toResponse(updated));
    }

    // ----------------- DELETE -----------------
    @DeleteMapping("/{word}")
    @Operation(summary = "Delete a sensitive word")
    public ResponseEntity<Void> deleteWord(@PathVariable String word) {
        wordService.deleteWordByWord(word);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("cache/refresh")
    @Operation(
            summary = "Refresh sensitive words cache",
            description = "Reloads the in-memory sensitive word pattern cache from the database without restarting the service."
    )
    @ApiResponse(responseCode = "200", description = "Cache refreshed successfully")
    public ResponseEntity<Map<String, Object>> refreshCache() {
        wordService.refreshCache();
        return ResponseEntity.ok(Map.of(
                "status", "refreshed",
                "timestamp", Instant.now().toString()
        ));
    }
}