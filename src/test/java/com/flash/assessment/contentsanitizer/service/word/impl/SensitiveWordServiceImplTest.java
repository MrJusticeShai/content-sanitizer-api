package com.flash.assessment.contentsanitizer.service.word.impl;

import com.flash.assessment.contentsanitizer.dto.CreateSensitiveWordRequest;
import com.flash.assessment.contentsanitizer.dto.UpdateSensitiveWordRequest;
import com.flash.assessment.contentsanitizer.entity.SensitiveWord;
import com.flash.assessment.contentsanitizer.exception.BadRequestException;
import com.flash.assessment.contentsanitizer.exception.ConflictException;
import com.flash.assessment.contentsanitizer.exception.NotFoundException;
import com.flash.assessment.contentsanitizer.repository.SensitiveWordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SensitiveWordServiceImplTest {

    @Mock
    private SensitiveWordRepository repository;

    @InjectMocks
    private SensitiveWordServiceImpl sensitiveWordService;

    private SensitiveWord mockWord;
    private CreateSensitiveWordRequest createRequest;
    private UpdateSensitiveWordRequest updateRequest;

    @BeforeEach
    void setUp() {
        mockWord = SensitiveWord.builder()
                .id(1L)
                .word("PASSWORD")
                .build();

        createRequest = new CreateSensitiveWordRequest();
        createRequest.setWord("password");

        updateRequest = new UpdateSensitiveWordRequest();
        updateRequest.setCurrentWord("PASSWORD");
        updateRequest.setNewWord("PASSPHRASE");
    }

    // refreshCache() / getCachedPatterns()

    @Nested
    @DisplayName("refreshCache() and getCachedPatterns() Logic")
    class CacheTests {

        @Test
        @DisplayName("Positive: Should populate cache with compiled patterns from all words")
        void refreshCache_PopulatesCache_Success() {
            when(repository.findAll()).thenReturn(List.of(mockWord));

            sensitiveWordService.refreshCache();
            List<Pattern> patterns = sensitiveWordService.getCachedPatterns();

            assertNotNull(patterns);
            assertEquals(1, patterns.size());
            verify(repository, times(1)).findAll();
        }

        @Test
        @DisplayName("Positive: Should result in empty cache when no words exist")
        void refreshCache_NoWords_EmptyCache() {
            when(repository.findAll()).thenReturn(List.of());

            sensitiveWordService.refreshCache();
            List<Pattern> patterns = sensitiveWordService.getCachedPatterns();

            assertNotNull(patterns);
            assertEquals(0, patterns.size());
            verify(repository, times(1)).findAll();
        }

        @Test
        @DisplayName("Positive: Should retain existing cache when repository throws during refresh")
        void refreshCache_RepositoryThrows_RetainsExistingCache() {
            when(repository.findAll()).thenReturn(List.of(mockWord));
            sensitiveWordService.refreshCache();
            List<Pattern> patternsBefore = sensitiveWordService.getCachedPatterns();
            assertEquals(1, patternsBefore.size());

            when(repository.findAll()).thenThrow(new RuntimeException("DB unavailable"));
            sensitiveWordService.refreshCache();

            List<Pattern> patternsAfter = sensitiveWordService.getCachedPatterns();
            assertEquals(1, patternsAfter.size());
        }

        @Test
        @DisplayName("Positive: Should return empty list before first cache load")
        void getCachedPatterns_BeforeRefresh_ReturnsEmptyList() {
            List<Pattern> patterns = sensitiveWordService.getCachedPatterns();

            assertNotNull(patterns);
            assertEquals(0, patterns.size());
            verifyNoInteractions(repository);
        }
    }

    // createWord()

    @Nested
    @DisplayName("createWord() Logic")
    class CreateWordTests {

        @Test
        @DisplayName("Positive: Should create and save word in uppercase")
        void createWord_ValidRequest_Success() {
            when(repository.existsByWordIgnoreCase("password")).thenReturn(false);
            when(repository.save(any(SensitiveWord.class))).thenReturn(mockWord);
            when(repository.findAll()).thenReturn(List.of(mockWord));

            SensitiveWord result = sensitiveWordService.createWord(createRequest);

            assertNotNull(result);
            verify(repository, times(1)).existsByWordIgnoreCase("password");
            verify(repository, times(1)).save(any(SensitiveWord.class));
        }

        @Test
        @DisplayName("Positive: Should trim whitespace before saving")
        void createWord_TrimsWhitespace_Success() {
            createRequest.setWord("  password  ");

            when(repository.existsByWordIgnoreCase("password")).thenReturn(false);
            when(repository.save(any(SensitiveWord.class))).thenAnswer(invocation -> {
                SensitiveWord saved = invocation.getArgument(0);
                assertEquals("PASSWORD", saved.getWord());
                return saved;
            });
            when(repository.findAll()).thenReturn(List.of(mockWord));

            sensitiveWordService.createWord(createRequest);

            verify(repository, times(1)).save(any(SensitiveWord.class));
        }

        @Test
        @DisplayName("Positive: Should refresh cache after creating a word")
        void createWord_RefreshesCache_AfterSave() {
            when(repository.existsByWordIgnoreCase("password")).thenReturn(false);
            when(repository.save(any(SensitiveWord.class))).thenReturn(mockWord);
            when(repository.findAll()).thenReturn(List.of(mockWord));

            sensitiveWordService.createWord(createRequest);

            verify(repository, times(1)).findAll();
        }

        @Test
        @DisplayName("Negative: Should throw ConflictException when word already exists")
        void createWord_DuplicateWord_ThrowsConflict() {
            when(repository.existsByWordIgnoreCase("password")).thenReturn(true);

            assertThrows(ConflictException.class, () ->
                    sensitiveWordService.createWord(createRequest));

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Negative: Should throw BadRequestException when word is blank")
        void createWord_BlankWord_ThrowsBadRequest() {
            createRequest.setWord("   ");

            assertThrows(BadRequestException.class, () ->
                    sensitiveWordService.createWord(createRequest));

            verifyNoInteractions(repository);
        }
    }

    // getAllWords()

    @Nested
    @DisplayName("getAllWords() Logic")
    class GetAllWordsTests {

        @Test
        @DisplayName("Positive: Should return all words from repository")
        void getAllWords_ReturnsAllWords_Success() {
            when(repository.findAll()).thenReturn(List.of(mockWord));

            List<SensitiveWord> result = sensitiveWordService.getAllWords();

            assertEquals(1, result.size());
            assertEquals("PASSWORD", result.get(0).getWord());
            verify(repository, times(1)).findAll();
        }

        @Test
        @DisplayName("Positive: Should return empty list when no words exist")
        void getAllWords_NoWords_ReturnsEmptyList() {
            when(repository.findAll()).thenReturn(List.of());

            List<SensitiveWord> result = sensitiveWordService.getAllWords();

            assertNotNull(result);
            assertEquals(0, result.size());
            verify(repository, times(1)).findAll();
        }
    }

    // updateWord()

    @Nested
    @DisplayName("updateWord() Logic")
    class UpdateWordTests {

        @Test
        @DisplayName("Positive: Should update word and save in uppercase")
        void updateWord_ValidRequest_Success() {
            when(repository.findByWordIgnoreCase("PASSWORD")).thenReturn(Optional.of(mockWord));
            when(repository.existsByWordIgnoreCase("PASSPHRASE")).thenReturn(false);
            when(repository.save(any(SensitiveWord.class))).thenAnswer(i -> i.getArgument(0));
            when(repository.findAll()).thenReturn(List.of(mockWord));

            SensitiveWord result = sensitiveWordService.updateWord(updateRequest);

            assertNotNull(result);
            assertEquals("PASSPHRASE", result.getWord());
            verify(repository, times(1)).findByWordIgnoreCase("PASSWORD");
            verify(repository, times(1)).existsByWordIgnoreCase("PASSPHRASE");
            verify(repository, times(1)).save(any(SensitiveWord.class));
        }

        @Test
        @DisplayName("Positive: Should refresh cache after updating a word")
        void updateWord_RefreshesCache_AfterUpdate() {
            when(repository.findByWordIgnoreCase("PASSWORD")).thenReturn(Optional.of(mockWord));
            when(repository.existsByWordIgnoreCase("PASSPHRASE")).thenReturn(false);
            when(repository.save(any(SensitiveWord.class))).thenAnswer(i -> i.getArgument(0));
            when(repository.findAll()).thenReturn(List.of(mockWord));

            sensitiveWordService.updateWord(updateRequest);

            verify(repository, times(1)).findAll();
        }

        @Test
        @DisplayName("Negative: Should throw BadRequestException when new word is same as current word")
        void updateWord_SameWord_ThrowsBadRequest() {
            updateRequest.setCurrentWord("PASSWORD");
            updateRequest.setNewWord("password");

            assertThrows(BadRequestException.class, () ->
                    sensitiveWordService.updateWord(updateRequest));

            verifyNoInteractions(repository);
        }

        @Test
        @DisplayName("Negative: Should throw NotFoundException when current word does not exist")
        void updateWord_WordNotFound_ThrowsNotFound() {
            when(repository.findByWordIgnoreCase("PASSWORD")).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () ->
                    sensitiveWordService.updateWord(updateRequest));

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Negative: Should throw ConflictException when new word already exists")
        void updateWord_NewWordConflict_ThrowsConflict() {
            when(repository.findByWordIgnoreCase("PASSWORD")).thenReturn(Optional.of(mockWord));
            when(repository.existsByWordIgnoreCase("PASSPHRASE")).thenReturn(true);

            assertThrows(ConflictException.class, () ->
                    sensitiveWordService.updateWord(updateRequest));

            verify(repository, never()).save(any());
        }
    }

    // deleteWordByWord()

    @Nested
    @DisplayName("deleteWordByWord() Logic")
    class DeleteWordTests {

        @Test
        @DisplayName("Positive: Should delete word and refresh cache")
        void deleteWordByWord_ExistingWord_Success() {
            when(repository.existsByWordIgnoreCase("PASSWORD")).thenReturn(true);
            doNothing().when(repository).deleteByWordIgnoreCase("PASSWORD");
            when(repository.findAll()).thenReturn(List.of());

            sensitiveWordService.deleteWordByWord("PASSWORD");

            verify(repository, times(1)).existsByWordIgnoreCase("PASSWORD");
            verify(repository, times(1)).deleteByWordIgnoreCase("PASSWORD");
        }

        @Test
        @DisplayName("Positive: Should refresh cache after deleting a word")
        void deleteWordByWord_RefreshesCache_AfterDelete() {
            when(repository.existsByWordIgnoreCase("PASSWORD")).thenReturn(true);
            doNothing().when(repository).deleteByWordIgnoreCase("PASSWORD");
            when(repository.findAll()).thenReturn(List.of());

            sensitiveWordService.deleteWordByWord("PASSWORD");

            verify(repository, times(1)).findAll();
        }

        @Test
        @DisplayName("Negative: Should throw NotFoundException when word does not exist")
        void deleteWordByWord_WordNotFound_ThrowsNotFound() {
            when(repository.existsByWordIgnoreCase("PASSWORD")).thenReturn(false);

            assertThrows(NotFoundException.class, () ->
                    sensitiveWordService.deleteWordByWord("PASSWORD"));

            verify(repository, never()).deleteByWordIgnoreCase(any());
        }
    }
}