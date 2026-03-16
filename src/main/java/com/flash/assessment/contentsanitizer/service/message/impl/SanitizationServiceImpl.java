package com.flash.assessment.contentsanitizer.service.message.impl;

import com.flash.assessment.contentsanitizer.exception.BadRequestException;
import com.flash.assessment.contentsanitizer.service.message.SanitizationService;
import com.flash.assessment.contentsanitizer.service.word.SensitiveWordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Implementation of {@link SanitizationService} that sanitizes messages
 * by masking sensitive words.
 *
 * <p>
 * This service retrieves all sensitive words from the {@link SensitiveWordService}
 * and replaces occurrences in the input message with asterisks.
 * </p>
 *
 * <p>
 * Example:
 * <pre>
 * Input: "My password is secret"
 * Sensitive words: ["password", "secret"]
 * Output: "My ******* is ******"
 * </pre>
 * </p>
 */
@Service
@RequiredArgsConstructor
public class SanitizationServiceImpl implements SanitizationService {

    private final SensitiveWordService wordService;

    @Override
    public String sanitizeMessage(String message) {
        if (!StringUtils.hasText(message)) throw new BadRequestException("Message must not be blank");

        List<Pattern> cachedPatterns = wordService.getCachedPatterns();

        String sanitized = message;
        for (Pattern pattern : cachedPatterns) {
            sanitized = pattern.matcher(sanitized)
                    .replaceAll(m -> "*".repeat(m.group().length()));
        }
        return sanitized;
    }
}