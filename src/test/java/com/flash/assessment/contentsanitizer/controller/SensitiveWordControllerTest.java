package com.flash.assessment.contentsanitizer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flash.assessment.contentsanitizer.dto.CreateSensitiveWordRequest;
import com.flash.assessment.contentsanitizer.dto.SensitiveWordResponse;
import com.flash.assessment.contentsanitizer.dto.UpdateSensitiveWordRequest;
import com.flash.assessment.contentsanitizer.entity.SensitiveWord;
import com.flash.assessment.contentsanitizer.exception.BadRequestException;
import com.flash.assessment.contentsanitizer.exception.ConflictException;
import com.flash.assessment.contentsanitizer.exception.GlobalExceptionHandler;
import com.flash.assessment.contentsanitizer.exception.NotFoundException;
import com.flash.assessment.contentsanitizer.mapper.SensitiveWordMapper;
import com.flash.assessment.contentsanitizer.service.word.SensitiveWordService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SensitiveWordController.class)
@Import(GlobalExceptionHandler.class)
class SensitiveWordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SensitiveWordService wordService;

    @MockBean
    private SensitiveWordMapper mapper;

    // --- Test data ---
    private final String WORD_VALUE = "PASSWORD";

    private SensitiveWord buildEntity(String word) {
        return SensitiveWord.builder().word(word).build();
    }

    private SensitiveWordResponse buildResponse(String word) {
        SensitiveWordResponse response = new SensitiveWordResponse();
        response.setWord(word);
        return response;
    }

    @Nested
    @DisplayName("POST /api/internal/sensitive-words")
    class CreateWordTests {

        @Test
        @DisplayName("Should return 200 and the created word")
        void createWord_ValidRequest_ReturnsOk() throws Exception {
            CreateSensitiveWordRequest request = new CreateSensitiveWordRequest();
            request.setWord(WORD_VALUE);

            SensitiveWord entity = buildEntity(WORD_VALUE);
            SensitiveWordResponse response = buildResponse(WORD_VALUE);

            when(wordService.createWord(any(CreateSensitiveWordRequest.class))).thenReturn(entity);
            when(mapper.toResponse(entity)).thenReturn(response);

            mockMvc.perform(post("/api/internal/sensitive-words")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.word").value(WORD_VALUE));

            verify(wordService, times(1)).createWord(any(CreateSensitiveWordRequest.class));
            verify(mapper, times(1)).toResponse(entity);
            verifyNoMoreInteractions(wordService, mapper);
        }

        @Test
        @DisplayName("Should return 400 when word is blank")
        void createWord_BlankWord_ReturnsBadRequest() throws Exception {
            CreateSensitiveWordRequest request = new CreateSensitiveWordRequest();
            request.setWord("");

            mockMvc.perform(post("/api/internal/sensitive-words")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());

            verifyNoInteractions(wordService, mapper);
        }

        @Test
        @DisplayName("Should return 400 when word is null")
        void createWord_NullWord_ReturnsBadRequest() throws Exception {
            CreateSensitiveWordRequest request = new CreateSensitiveWordRequest();

            mockMvc.perform(post("/api/internal/sensitive-words")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());

            verifyNoInteractions(wordService, mapper);
        }

        @Test
        @DisplayName("Should return 409 when word already exists")
        void createWord_DuplicateWord_ReturnsConflict() throws Exception {
            CreateSensitiveWordRequest request = new CreateSensitiveWordRequest();
            request.setWord(WORD_VALUE);

            when(wordService.createWord(any(CreateSensitiveWordRequest.class)))
                    .thenThrow(new ConflictException("Sensitive word already exists: " + WORD_VALUE));

            mockMvc.perform(post("/api/internal/sensitive-words")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").exists());

            verify(wordService, times(1)).createWord(any(CreateSensitiveWordRequest.class));
            verifyNoMoreInteractions(wordService);
            verifyNoInteractions(mapper);
        }
    }

    @Nested
    @DisplayName("POST /api/internal/sensitive-words/bulk")
    class BulkCreateWordTests {

        @Test
        @DisplayName("Should return 200 and list of created words")
        void bulkCreateWords_ValidRequest_ReturnsOk() throws Exception {
            CreateSensitiveWordRequest req1 = new CreateSensitiveWordRequest();
            req1.setWord("password");
            CreateSensitiveWordRequest req2 = new CreateSensitiveWordRequest();
            req2.setWord("secret");

            SensitiveWord entity1 = buildEntity("PASSWORD");
            SensitiveWord entity2 = buildEntity("SECRET");
            SensitiveWordResponse response1 = buildResponse("PASSWORD");
            SensitiveWordResponse response2 = buildResponse("SECRET");

            when(wordService.bulkCreateWords(anyList())).thenReturn(List.of(entity1, entity2));
            when(mapper.toResponse(entity1)).thenReturn(response1);
            when(mapper.toResponse(entity2)).thenReturn(response2);

            mockMvc.perform(post("/api/internal/sensitive-words/bulk")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(List.of(req1, req2))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].word").value("PASSWORD"))
                    .andExpect(jsonPath("$[1].word").value("SECRET"));

            verify(wordService, times(1)).bulkCreateWords(anyList());
            verify(mapper, times(1)).toResponse(entity1);
            verify(mapper, times(1)).toResponse(entity2);
            verifyNoMoreInteractions(wordService, mapper);
        }

        @Test
        @DisplayName("Should return 200 with only newly created words when some already exist")
        void bulkCreateWords_SomeExist_ReturnsOnlyCreated() throws Exception {
            CreateSensitiveWordRequest req1 = new CreateSensitiveWordRequest();
            req1.setWord("password");
            CreateSensitiveWordRequest req2 = new CreateSensitiveWordRequest();
            req2.setWord("secret");

            SensitiveWord entity1 = buildEntity("PASSWORD");
            SensitiveWordResponse response1 = buildResponse("PASSWORD");

            when(wordService.bulkCreateWords(anyList())).thenReturn(List.of(entity1));
            when(mapper.toResponse(entity1)).thenReturn(response1);

            mockMvc.perform(post("/api/internal/sensitive-words/bulk")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(List.of(req1, req2))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].word").value("PASSWORD"));

            verify(wordService, times(1)).bulkCreateWords(anyList());
            verifyNoMoreInteractions(wordService);
        }

        @Test
        @DisplayName("Should return 200 with empty list when all words already exist")
        void bulkCreateWords_AllExist_ReturnsEmptyList() throws Exception {
            CreateSensitiveWordRequest req1 = new CreateSensitiveWordRequest();
            req1.setWord("password");

            when(wordService.bulkCreateWords(anyList())).thenReturn(List.of());

            mockMvc.perform(post("/api/internal/sensitive-words/bulk")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(List.of(req1))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(wordService, times(1)).bulkCreateWords(anyList());
            verifyNoMoreInteractions(wordService);
            verifyNoInteractions(mapper);
        }

        @Test
        @DisplayName("Should return 400 when request list is empty")
        void bulkCreateWords_EmptyList_ReturnsBadRequest() throws Exception {
            when(wordService.bulkCreateWords(anyList()))
                    .thenThrow(new BadRequestException("Word list must not be empty"));

            mockMvc.perform(post("/api/internal/sensitive-words/bulk")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(List.of())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());

            verify(wordService, times(1)).bulkCreateWords(anyList());
            verifyNoMoreInteractions(wordService);
            verifyNoInteractions(mapper);
        }
    }

    @Nested
    @DisplayName("GET /api/internal/sensitive-words")
    class GetAllWordsTests {

        @Test
        @DisplayName("Should return 200 and a list of all words")
        void getAllWords_ReturnsOk() throws Exception {
            SensitiveWord entity1 = buildEntity("PASSWORD");
            SensitiveWord entity2 = buildEntity("SECRET");
            SensitiveWordResponse response1 = buildResponse("PASSWORD");
            SensitiveWordResponse response2 = buildResponse("SECRET");

            when(wordService.getAllWords()).thenReturn(List.of(entity1, entity2));
            when(mapper.toResponse(entity1)).thenReturn(response1);
            when(mapper.toResponse(entity2)).thenReturn(response2);

            mockMvc.perform(get("/api/internal/sensitive-words"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].word").value("PASSWORD"))
                    .andExpect(jsonPath("$[1].word").value("SECRET"));

            verify(wordService, times(1)).getAllWords();
            verify(mapper, times(1)).toResponse(entity1);
            verify(mapper, times(1)).toResponse(entity2);
            verifyNoMoreInteractions(wordService, mapper);
        }

        @Test
        @DisplayName("Should return 200 and an empty list when no words exist")
        void getAllWords_EmptyList_ReturnsOk() throws Exception {
            when(wordService.getAllWords()).thenReturn(List.of());

            mockMvc.perform(get("/api/internal/sensitive-words"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(wordService, times(1)).getAllWords();
            verifyNoMoreInteractions(wordService);
            verifyNoInteractions(mapper);
        }
    }

    @Nested
    @DisplayName("PUT /api/internal/sensitive-words")
    class UpdateWordTests {

        @Test
        @DisplayName("Should return 200 and the updated word")
        void updateWord_ValidRequest_ReturnsOk() throws Exception {
            UpdateSensitiveWordRequest request = new UpdateSensitiveWordRequest();
            request.setCurrentWord("PASSWORD");
            request.setNewWord("PASSPHRASE");

            SensitiveWord updated = buildEntity("PASSPHRASE");
            SensitiveWordResponse response = buildResponse("PASSPHRASE");

            when(wordService.updateWord(any(UpdateSensitiveWordRequest.class))).thenReturn(updated);
            when(mapper.toResponse(updated)).thenReturn(response);

            mockMvc.perform(put("/api/internal/sensitive-words")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.word").value("PASSPHRASE"));

            verify(wordService, times(1)).updateWord(any(UpdateSensitiveWordRequest.class));
            verify(mapper, times(1)).toResponse(updated);
            verifyNoMoreInteractions(wordService, mapper);
        }

        @Test
        @DisplayName("Should return 404 when word to update does not exist")
        void updateWord_WordNotFound_ReturnsNotFound() throws Exception {
            UpdateSensitiveWordRequest request = new UpdateSensitiveWordRequest();
            request.setCurrentWord("GHOST");
            request.setNewWord("PHANTOM");

            when(wordService.updateWord(any(UpdateSensitiveWordRequest.class)))
                    .thenThrow(new NotFoundException("Word not found: GHOST"));

            mockMvc.perform(put("/api/internal/sensitive-words")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").exists());

            verify(wordService, times(1)).updateWord(any(UpdateSensitiveWordRequest.class));
            verifyNoMoreInteractions(wordService);
            verifyNoInteractions(mapper);
        }

        @Test
        @DisplayName("Should return 409 when new word already exists")
        void updateWord_NewWordConflict_ReturnsConflict() throws Exception {
            UpdateSensitiveWordRequest request = new UpdateSensitiveWordRequest();
            request.setCurrentWord("PASSWORD");
            request.setNewWord("SECRET");

            when(wordService.updateWord(any(UpdateSensitiveWordRequest.class)))
                    .thenThrow(new ConflictException("Sensitive word already exists: SECRET"));

            mockMvc.perform(put("/api/internal/sensitive-words")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").exists());

            verify(wordService, times(1)).updateWord(any(UpdateSensitiveWordRequest.class));
            verifyNoMoreInteractions(wordService);
            verifyNoInteractions(mapper);
        }

        @Test
        @DisplayName("Should return 400 when request fields are blank")
        void updateWord_BlankFields_ReturnsBadRequest() throws Exception {
            UpdateSensitiveWordRequest request = new UpdateSensitiveWordRequest();
            request.setCurrentWord("");
            request.setNewWord("");

            mockMvc.perform(put("/api/internal/sensitive-words")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());

            verifyNoInteractions(wordService, mapper);
        }
    }

    @Nested
    @DisplayName("DELETE /api/internal/sensitive-words/{word}")
    class DeleteWordTests {

        @Test
        @DisplayName("Should return 204 when word is deleted successfully")
        void deleteWord_ExistingWord_ReturnsNoContent() throws Exception {
            doNothing().when(wordService).deleteWordByWord(WORD_VALUE);

            mockMvc.perform(delete("/api/internal/sensitive-words/{word}", WORD_VALUE))
                    .andExpect(status().isNoContent());

            verify(wordService, times(1)).deleteWordByWord(WORD_VALUE);
            verifyNoMoreInteractions(wordService);
            verifyNoInteractions(mapper);
        }

        @Test
        @DisplayName("Should return 404 when word to delete does not exist")
        void deleteWord_WordNotFound_ReturnsNotFound() throws Exception {
            doThrow(new NotFoundException("Word not found: " + WORD_VALUE))
                    .when(wordService).deleteWordByWord(WORD_VALUE);

            mockMvc.perform(delete("/api/internal/sensitive-words/{word}", WORD_VALUE))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").exists());

            verify(wordService, times(1)).deleteWordByWord(WORD_VALUE);
            verifyNoMoreInteractions(wordService);
            verifyNoInteractions(mapper);
        }
    }

    @Nested
    @DisplayName("POST /api/internal/sensitive-words/cache/refresh")
    class CacheRefreshTests {

        @Test
        @DisplayName("Should return 200 with status and timestamp")
        void refreshCache_ReturnsOk() throws Exception {
            doNothing().when(wordService).refreshCache();

            mockMvc.perform(post("/api/internal/sensitive-words/cache/refresh"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("refreshed"))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(wordService, times(1)).refreshCache();
            verifyNoMoreInteractions(wordService);
            verifyNoInteractions(mapper);
        }
    }
}