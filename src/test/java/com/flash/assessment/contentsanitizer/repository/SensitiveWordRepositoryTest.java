package com.flash.assessment.contentsanitizer.repository;

import com.flash.assessment.contentsanitizer.entity.SensitiveWord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class SensitiveWordRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SensitiveWordRepository repository;

    private SensitiveWord persistedWord;

    @BeforeEach
    void setUp() {
        persistedWord = entityManager.persistAndFlush(
                SensitiveWord.builder().word("PASSWORD").build()
        );
    }

    // findByWordIgnoreCase()

    @Nested
    @DisplayName("findByWordIgnoreCase() Logic")
    class FindByWordIgnoreCaseTests {

        @Test
        @DisplayName("Positive: Should find word with exact case match")
        void findByWordIgnoreCase_ExactCase_Success() {
            Optional<SensitiveWord> result = repository.findByWordIgnoreCase("PASSWORD");

            assertTrue(result.isPresent());
            assertEquals("PASSWORD", result.get().getWord());
        }

        @Test
        @DisplayName("Positive: Should find word with lowercase input")
        void findByWordIgnoreCase_Lowercase_Success() {
            Optional<SensitiveWord> result = repository.findByWordIgnoreCase("password");

            assertTrue(result.isPresent());
            assertEquals("PASSWORD", result.get().getWord());
        }

        @Test
        @DisplayName("Positive: Should find word with mixed case input")
        void findByWordIgnoreCase_MixedCase_Success() {
            Optional<SensitiveWord> result = repository.findByWordIgnoreCase("PaSsWoRd");

            assertTrue(result.isPresent());
            assertEquals("PASSWORD", result.get().getWord());
        }

        @Test
        @DisplayName("Negative: Should return empty Optional when word does not exist")
        void findByWordIgnoreCase_WordNotFound_ReturnsEmpty() {
            Optional<SensitiveWord> result = repository.findByWordIgnoreCase("GHOST");

            assertFalse(result.isPresent());
        }
    }

    // existsByWordIgnoreCase()

    @Nested
    @DisplayName("existsByWordIgnoreCase() Logic")
    class ExistsByWordIgnoreCaseTests {

        @Test
        @DisplayName("Positive: Should return true when word exists with exact case")
        void existsByWordIgnoreCase_ExactCase_ReturnsTrue() {
            boolean exists = repository.existsByWordIgnoreCase("PASSWORD");

            assertTrue(exists);
        }

        @Test
        @DisplayName("Positive: Should return true when word exists with lowercase input")
        void existsByWordIgnoreCase_Lowercase_ReturnsTrue() {
            boolean exists = repository.existsByWordIgnoreCase("password");

            assertTrue(exists);
        }

        @Test
        @DisplayName("Positive: Should return true when word exists with mixed case input")
        void existsByWordIgnoreCase_MixedCase_ReturnsTrue() {
            boolean exists = repository.existsByWordIgnoreCase("PaSsWoRd");

            assertTrue(exists);
        }

        @Test
        @DisplayName("Negative: Should return false when word does not exist")
        void existsByWordIgnoreCase_WordNotFound_ReturnsFalse() {
            boolean exists = repository.existsByWordIgnoreCase("GHOST");

            assertFalse(exists);
        }
    }

    // deleteByWordIgnoreCase()

    @Nested
    @DisplayName("deleteByWordIgnoreCase() Logic")
    class DeleteByWordIgnoreCaseTests {

        @Test
        @DisplayName("Positive: Should delete word with exact case match")
        void deleteByWordIgnoreCase_ExactCase_Success() {
            repository.deleteByWordIgnoreCase("PASSWORD");
            entityManager.flush();

            Optional<SensitiveWord> result = repository.findByWordIgnoreCase("PASSWORD");
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Positive: Should delete word with lowercase input")
        void deleteByWordIgnoreCase_Lowercase_Success() {
            repository.deleteByWordIgnoreCase("password");
            entityManager.flush();

            Optional<SensitiveWord> result = repository.findByWordIgnoreCase("PASSWORD");
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Positive: Should delete word with mixed case input")
        void deleteByWordIgnoreCase_MixedCase_Success() {
            repository.deleteByWordIgnoreCase("PaSsWoRd");
            entityManager.flush();

            Optional<SensitiveWord> result = repository.findByWordIgnoreCase("PASSWORD");
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Positive: Should not throw when word does not exist")
        void deleteByWordIgnoreCase_WordNotFound_DoesNotThrow() {
            assertDoesNotThrow(() -> {
                repository.deleteByWordIgnoreCase("GHOST");
                entityManager.flush();
            });
        }

        @Test
        @DisplayName("Positive: Should only delete the targeted word, leaving others intact")
        void deleteByWordIgnoreCase_OnlyDeletesTargetWord() {
            entityManager.persistAndFlush(
                    SensitiveWord.builder().word("SECRET").build()
            );

            repository.deleteByWordIgnoreCase("PASSWORD");
            entityManager.flush();

            assertFalse(repository.findByWordIgnoreCase("PASSWORD").isPresent());
            assertTrue(repository.findByWordIgnoreCase("SECRET").isPresent());
        }
    }
}