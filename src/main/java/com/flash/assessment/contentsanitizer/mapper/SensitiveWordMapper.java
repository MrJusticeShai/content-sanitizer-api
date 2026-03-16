package com.flash.assessment.contentsanitizer.mapper;

import com.flash.assessment.contentsanitizer.dto.SensitiveWordResponse;
import com.flash.assessment.contentsanitizer.entity.SensitiveWord;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting SensitiveWord entity to SensitiveWordResponse DTO.
 *
 * <p>
 * Masks the word in the response to avoid leaking sensitive content.
 * </p>
 */
@Component
public class SensitiveWordMapper {

    public SensitiveWordResponse toResponse(SensitiveWord word) {
        return SensitiveWordResponse.builder()
                .id(word.getId())
                .wordMasked("*".repeat(word.getWord().length()))
                .build();
    }
}