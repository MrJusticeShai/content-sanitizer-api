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
     * @return an Optional containing the entity if found
     */
    Optional<SensitiveWord> findByWordIgnoreCase(String word);

    boolean existsByWordIgnoreCase(String word);

    void deleteByWordIgnoreCase(String word);

}
