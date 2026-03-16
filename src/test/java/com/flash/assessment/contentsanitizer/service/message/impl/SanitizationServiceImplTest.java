package com.flash.assessment.contentsanitizer.service.message.impl;

import com.flash.assessment.contentsanitizer.exception.BadRequestException;
import com.flash.assessment.contentsanitizer.service.word.SensitiveWordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SanitizationServiceImplTest {

    @Mock
    private SensitiveWordService wordService;

    @InjectMocks
    private SanitizationServiceImpl sanitizationService;

    private List<Pattern> mockPatterns;

    @BeforeEach
    void setUp() {
        mockPatterns = List.of(
                Pattern.compile("(?i)\\b" + Pattern.quote("password") + "\\b"),
                Pattern.compile("(?i)\\b" + Pattern.quote("secret") + "\\b")
        );
    }

    @Nested
    @DisplayName("sanitizeMessage() Logic")
    class SanitizeMessageTests {

        @Test
        @DisplayName("Positive: Should mask a single sensitive word with asterisks")
        void sanitizeMessage_SingleSensitiveWord_Success() {
            when(wordService.getCachedPatterns()).thenReturn(mockPatterns);

            String result = sanitizationService.sanitizeMessage("My password is 12345");

            assertEquals("My ******** is 12345", result);
            verify(wordService, times(1)).getCachedPatterns();
            verifyNoMoreInteractions(wordService);
        }

        @Test
        @DisplayName("Positive: Should mask multiple sensitive words with asterisks")
        void sanitizeMessage_MultipleSensitiveWords_Success() {
            when(wordService.getCachedPatterns()).thenReturn(mockPatterns);

            String result = sanitizationService.sanitizeMessage("My password and secret are exposed");

            assertEquals("My ******** and ****** are exposed", result);
            verify(wordService, times(1)).getCachedPatterns();
            verifyNoMoreInteractions(wordService);
        }

        @Test
        @DisplayName("Positive: Should return message unchanged when no sensitive words match")
        void sanitizeMessage_NoSensitiveWords_ReturnsUnchanged() {
            when(wordService.getCachedPatterns()).thenReturn(mockPatterns);

            String input = "Hello world, nothing sensitive here";
            String result = sanitizationService.sanitizeMessage(input);

            assertEquals(input, result);
            verify(wordService, times(1)).getCachedPatterns();
            verifyNoMoreInteractions(wordService);
        }

        @Test
        @DisplayName("Positive: Should mask sensitive word regardless of casing")
        void sanitizeMessage_CaseInsensitiveMatch_Success() {
            when(wordService.getCachedPatterns()).thenReturn(mockPatterns);

            String result = sanitizationService.sanitizeMessage("My PASSWORD is exposed");

            assertEquals("My ******** is exposed", result);
            verify(wordService, times(1)).getCachedPatterns();
            verifyNoMoreInteractions(wordService);
        }

        @Test
        @DisplayName("Positive: Should preserve original word length when masking")
        void sanitizeMessage_MaskLengthMatchesWord_Success() {
            when(wordService.getCachedPatterns()).thenReturn(mockPatterns);

            String result = sanitizationService.sanitizeMessage("My secret is safe");

            assertEquals("My ****** is safe", result);
            verify(wordService, times(1)).getCachedPatterns();
            verifyNoMoreInteractions(wordService);
        }

        @Test
        @DisplayName("Positive: Should return message unchanged when pattern cache is empty")
        void sanitizeMessage_EmptyCache_ReturnsUnchanged() {
            when(wordService.getCachedPatterns()).thenReturn(List.of());

            String input = "My password is exposed";
            String result = sanitizationService.sanitizeMessage(input);

            assertEquals(input, result);
            verify(wordService, times(1)).getCachedPatterns();
            verifyNoMoreInteractions(wordService);
        }

        @Test
        @DisplayName("Positive: Should mask all occurrences of a sensitive word in the same message")
        void sanitizeMessage_RepeatedSensitiveWord_MasksAll() {
            when(wordService.getCachedPatterns()).thenReturn(mockPatterns);

            String result = sanitizationService.sanitizeMessage("password is not my password");

            assertEquals("******** is not my ********", result);
            verify(wordService, times(1)).getCachedPatterns();
            verifyNoMoreInteractions(wordService);
        }

        @Test
        @DisplayName("Negative: Should throw BadRequestException when message is null")
        void sanitizeMessage_NullMessage_ThrowsBadRequest() {
            assertThrows(BadRequestException.class, () ->
                    sanitizationService.sanitizeMessage(null));

            verifyNoInteractions(wordService);
        }

        @Test
        @DisplayName("Negative: Should throw BadRequestException when message is blank")
        void sanitizeMessage_BlankMessage_ThrowsBadRequest() {
            assertThrows(BadRequestException.class, () ->
                    sanitizationService.sanitizeMessage("   "));

            verifyNoInteractions(wordService);
        }

        @Test
        @DisplayName("Negative: Should throw BadRequestException when message is empty")
        void sanitizeMessage_EmptyMessage_ThrowsBadRequest() {
            assertThrows(BadRequestException.class, () ->
                    sanitizationService.sanitizeMessage(""));

            verifyNoInteractions(wordService);
        }
    }
}