package com.flash.assessment.contentsanitizer.service.message.impl;

import com.flash.assessment.contentsanitizer.entity.SensitiveWord;
import com.flash.assessment.contentsanitizer.service.message.SanitizationService;
import com.flash.assessment.contentsanitizer.service.word.SensitiveWordService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
    private volatile List<SensitiveWord> cachedWords;

    @PostConstruct
    public void init() {
        refreshCache();
    }

    public void refreshCache() {
        cachedWords = wordService.getAllWords();
    }

    @Override
    public String sanitizeMessage(String message) {
        if (message == null || message.isEmpty()) return message;
        String sanitized = message;
        for (SensitiveWord word : cachedWords) {
            sanitized = sanitized.replaceAll("(?i)\\b" + word.getWord() + "\\b",
                    "*".repeat(word.getWord().length()));
        }
        return sanitized;
    }
}