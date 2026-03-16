package com.flash.assessment.contentsanitizer.service.message;

/**
 * Service interface for sanitizing messages by removing or masking sensitive words.
 *
 * <p>
 * Implementations of this interface should provide the logic for processing
 * input messages and returning a sanitized version that complies with
 * content safety rules.
 * </p>
 */
public interface SanitizationService {

    /**
     * Sanitizes the given message by detecting and removing or masking sensitive words.
     *
     * @param message the input message to be sanitized; must not be null or empty
     * @return the sanitized message with sensitive words removed or masked
     */
    String sanitizeMessage(String message);

    /**
     * Reloads the sensitive word patterns from the database into the in-memory cache.
     * Safe to call at runtime without restarting the service.
     */
    void refreshCache();
}
