package com.flash.assessment.contentsanitizer.service.message.impl;

import com.flash.assessment.contentsanitizer.entity.SensitiveWord;
import com.flash.assessment.contentsanitizer.service.message.SanitizationService;
import com.flash.assessment.contentsanitizer.service.word.SensitiveWordService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
@Slf4j
@RequiredArgsConstructor
public class SanitizationServiceImpl implements SanitizationService {

    private final SensitiveWordService wordService;
    private final AtomicReference<List<Pattern>> cachedPatterns = new AtomicReference<>(List.of());

    @PostConstruct
    @Override
    public void refreshCache() {
        try {
            List<Pattern> patterns = wordService.getAllWords().stream()
                    .map(SensitiveWord::getWord)
                    .filter(StringUtils::hasText)
                    .map(word -> Pattern.compile("(?i)\\b" + Pattern.quote(word) + "\\b"))
                    .toList();

            cachedPatterns.set(patterns);
            log.info("Sanitization cache refreshed — {} pattern(s) loaded.", patterns.size());
        } catch (Exception e) {
            log.error("Cache refresh failed — retaining existing {} pattern(s).", cachedPatterns.get().size(), e);
        }
    }

    @Override
    public String sanitizeMessage(String message) {
        if (!StringUtils.hasText(message)) return message;

        String sanitized = message;
        for (Pattern pattern : cachedPatterns.get()) {
            sanitized = pattern.matcher(sanitized)
                    .replaceAll(m -> "*".repeat(m.group().length()));
        }
        return sanitized;
    }
}