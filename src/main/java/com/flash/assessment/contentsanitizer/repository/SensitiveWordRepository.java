package com.flash.assessment.contentsanitizer.repository;

import com.flash.assessment.contentsanitizer.entity.SensitiveWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for performing CRUD operations on {@link SensitiveWord} entities.
 * <p>
 * Extends {@link JpaRepository} to provide standard JPA operations (save, findById, delete, etc.)
 * and declares custom query methods specific to bids.
 * </p>
 *
 * <p>
 *  Additional query methods can be defined here following Spring Data JPA
 *  naming conventions.
 *  </p>
 */
@Repository
public interface SensitiveWordRepository extends JpaRepository<SensitiveWord, Long> {

    /**
     * Finds a sensitive word by its value, case-insensitive.
     *
     * @param word the word to search for
     * @return an {@link Optional} containing the matching {@link SensitiveWord} if found,
     *         or an empty {@link Optional} if no match exists
     */
    Optional<SensitiveWord> findByWordIgnoreCase(String word);

    /**
     * Checks whether a sensitive word exists, case-insensitive.
     *
     * @param word the word to check
     * @return {@code true} if a matching word exists, {@code false} otherwise
     */
    boolean existsByWordIgnoreCase(String word);

    /**
     * Deletes a sensitive word by its value, case-insensitive.
     *
     * <p>
     * If no matching word is found, this method completes without throwing an exception.
     * </p>
     *
     * @param word the word to delete
     */
    void deleteByWordIgnoreCase(String word);

}
