package com.flash.assessment.contentsanitizer.controller;

import com.flash.assessment.contentsanitizer.dto.CreateSensitiveWordRequest;
import com.flash.assessment.contentsanitizer.dto.SanitizeResponse;
import com.flash.assessment.contentsanitizer.dto.SensitiveWordResponse;
import com.flash.assessment.contentsanitizer.dto.UpdateSensitiveWordRequest;
import com.flash.assessment.contentsanitizer.entity.SensitiveWord;
import com.flash.assessment.contentsanitizer.mapper.SensitiveWordMapper;
import com.flash.assessment.contentsanitizer.service.word.SensitiveWordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for managing sensitive words.
 * <p>
 * Provides CRUD endpoints using DTOs to interact with clients.
 * </p>
 */
@RestController
@RequestMapping("/api/words")
@RequiredArgsConstructor
@Tag(name = "Sensitive Words", description = "CRUD operations for managing sensitive words")
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
        wordService.deleteWord(word);
        return ResponseEntity.noContent().build();
    }
}