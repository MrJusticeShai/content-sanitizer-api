package com.flash.assessment.contentsanitizer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flash.assessment.contentsanitizer.dto.SanitizeRequest;
import com.flash.assessment.contentsanitizer.exception.GlobalExceptionHandler;
import com.flash.assessment.contentsanitizer.service.message.SanitizationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SanitizationController.class)
@Import(GlobalExceptionHandler.class)

class SanitizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SanitizationService sanitizationService;

    private final String SINGLE_RAW = "My password is 12345";
    private final String SINGLE_SANITIZED = "My ****** is 12345";

    private final String MULTIPLE_RAW = "My password and secret are exposed";
    private final String MULTIPLE_SANITIZED = "My ****** and ****** are exposed";

    private final String CLEAN_MESSAGE = "Hello world, nothing sensitive here";

    @Nested
    @DisplayName("POST /api/sanitize")
    class SanitizeMessageTests {

        @Test
        @DisplayName("Should return 200 and sanitize a single sensitive word")
        void sanitizeMessage_SingleSensitiveWord() throws Exception {

            SanitizeRequest request = new SanitizeRequest();
            request.setMessage(SINGLE_RAW);

            when(sanitizationService.sanitizeMessage(anyString()))
                    .thenReturn(SINGLE_SANITIZED);

            mockMvc.perform(post("/api/sanitize")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sanitizedMessage").value(SINGLE_SANITIZED));

            verify(sanitizationService, times(1)).sanitizeMessage(SINGLE_RAW);
            verifyNoMoreInteractions(sanitizationService);
        }

        @Test
        @DisplayName("Should return 200 and sanitize multiple sensitive words")
        void sanitizeMessage_MultipleSensitiveWords() throws Exception {

            SanitizeRequest request = new SanitizeRequest();
            request.setMessage(MULTIPLE_RAW);

            when(sanitizationService.sanitizeMessage(anyString()))
                    .thenReturn(MULTIPLE_SANITIZED);

            mockMvc.perform(post("/api/sanitize")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sanitizedMessage").value(MULTIPLE_SANITIZED));

            verify(sanitizationService, times(1)).sanitizeMessage(MULTIPLE_RAW);
            verifyNoMoreInteractions(sanitizationService);
        }

        @Test
        @DisplayName("Should return 200 and return message unchanged when no sensitive words match")
        void sanitizeMessage_NoSensitiveWords() throws Exception {

            SanitizeRequest request = new SanitizeRequest();
            request.setMessage(CLEAN_MESSAGE);

            when(sanitizationService.sanitizeMessage(anyString()))
                    .thenReturn(CLEAN_MESSAGE);

            mockMvc.perform(post("/api/sanitize")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sanitizedMessage").value(CLEAN_MESSAGE));

            verify(sanitizationService, times(1)).sanitizeMessage(CLEAN_MESSAGE);
            verifyNoMoreInteractions(sanitizationService);
        }

        @Test
        @DisplayName("Should return 400 when message is blank")
        void sanitizeMessage_BlankMessage_Fails() throws Exception {

            SanitizeRequest request = new SanitizeRequest();
            request.setMessage("");

            mockMvc.perform(post("/api/sanitize")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("message: must not be blank"));

            verifyNoInteractions(sanitizationService);
        }

        @Test
        @DisplayName("Should return 400 when message is null")
        void sanitizeMessage_NullMessage_Fails() throws Exception {

            SanitizeRequest request = new SanitizeRequest();

            mockMvc.perform(post("/api/sanitize")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("message: must not be blank"));

            verifyNoInteractions(sanitizationService);
        }
    }
}