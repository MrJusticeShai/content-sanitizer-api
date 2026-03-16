package com.flash.assessment.contentsanitizer.mapper;

import com.flash.assessment.contentsanitizer.dto.SensitiveWordResponse;
import com.flash.assessment.contentsanitizer.entity.SensitiveWord;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting {@link SensitiveWord} entities to {@link SensitiveWordResponse} DTOs.
 *
 * <p>
 * This mapper is intended for internal use within the sensitive words CRUD operations.
 * It exposes the actual word because these endpoints are internal and not public.
 * External clients should only receive masked words via the {@code sanitizeMessage} endpoint.
 * </p>
 */
@Component
public class SensitiveWordMapper {

    public SensitiveWordResponse toResponse(SensitiveWord word) {
        return SensitiveWordResponse.builder()
                .id(word.getId())
                .word(word.getWord())
                .build();
    }
}