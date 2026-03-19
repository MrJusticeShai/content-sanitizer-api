package com.flash.assessment.contentsanitizer.service.word;

import com.flash.assessment.contentsanitizer.dto.CreateSensitiveWordRequest;
import com.flash.assessment.contentsanitizer.dto.UpdateSensitiveWordRequest;
import com.flash.assessment.contentsanitizer.entity.SensitiveWord;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Service interface for managing sensitive words in the system.
 *
 * <p>
 * Provides operations to create, retrieve, update, and delete sensitive words.
 * Implementations handle the business logic and interaction with persistence layers.
 * </p>
 */
public interface SensitiveWordService {

    /**
     * Creates a new sensitive word in the system.
     *
     * @param createRequest the DTO containing the word to create
     * @return the created {@link SensitiveWord} entity
     */
    SensitiveWord createWord(CreateSensitiveWordRequest createRequest);


    /**
     * Creates multiple sensitive words in a single transaction.
     *
     * <p>
     * Words that already exist are skipped without throwing an exception.
     * The result contains only the words that were successfully created.
     * </p>
     *
     * @param requests list of DTOs containing the words to create
     * @return list of successfully created {@link SensitiveWord} entities
     */
    List<SensitiveWord> bulkCreateWords(List<CreateSensitiveWordRequest> requests);

    /**
     * Retrieves all sensitive words currently stored in the system.
     *
     * @return a list of all {@link SensitiveWord} entities
     */
    List<SensitiveWord> getAllWords();

    /**
     * Updates the word of an existing sensitive word entity.
     *
     * @param updateRequest  the DTO containing the word to update
     * @return the updated {@link SensitiveWord} entity
     */
    SensitiveWord updateWord(UpdateSensitiveWordRequest updateRequest);

    /**
     * Deletes a sensitive word from the system.
     *
     * @param word the ID of the sensitive word to delete
     */
    void deleteWordByWord(String word);

    /**
     * Reloads the sensitive word patterns from the database into the in-memory cache.
     * Safe to call at runtime without restarting the service.
     */
    void refreshCache();


    public List<Pattern> getCachedPatterns();

}
