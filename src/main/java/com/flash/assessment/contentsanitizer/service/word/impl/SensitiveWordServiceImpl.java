package com.flash.assessment.contentsanitizer.service.word.impl;

import com.flash.assessment.contentsanitizer.dto.CreateSensitiveWordRequest;
import com.flash.assessment.contentsanitizer.dto.UpdateSensitiveWordRequest;
import com.flash.assessment.contentsanitizer.entity.SensitiveWord;
import com.flash.assessment.contentsanitizer.exception.BadRequestException;
import com.flash.assessment.contentsanitizer.exception.ConflictException;
import com.flash.assessment.contentsanitizer.exception.NotFoundException;
import com.flash.assessment.contentsanitizer.repository.SensitiveWordRepository;
import com.flash.assessment.contentsanitizer.service.word.SensitiveWordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SensitiveWordServiceImpl implements SensitiveWordService {

    private final SensitiveWordRepository repository;

    @Override
    public SensitiveWord createWord(CreateSensitiveWordRequest createRequest) {
        String newWord = createRequest.getWord().trim();

        if (newWord.isBlank()) {
            throw new BadRequestException("Sensitive word cannot be empty");
        }

        boolean exists = repository.existsByWordIgnoreCase(newWord);
        if (exists) {
            throw new ConflictException("Sensitive word already exists: " + newWord);
        }

        SensitiveWord word = SensitiveWord.builder()
                .word(newWord.toUpperCase())
                .build();

        SensitiveWord saved = repository.save(word);

        return saved;
    }

    @Override
    public List<SensitiveWord> getAllWords() {
        return repository.findAll();
    }

    @Override
    public SensitiveWord updateWord(UpdateSensitiveWordRequest updateRequest) {
        String currentWord = updateRequest.getCurrentWord().trim();
        String newWord = updateRequest.getNewWord().trim().toUpperCase();

        if (currentWord.equalsIgnoreCase(newWord)) {
            throw new BadRequestException("The new word is the same as the current word");
        }

        // Find existing word
        SensitiveWord existing = repository.findByWordIgnoreCase(currentWord)
                .orElseThrow(() -> new NotFoundException("Word not found: " + currentWord));

        // Check if the new word already exists
        boolean exists = repository.existsByWordIgnoreCase(newWord);
        if (exists) {
            throw new ConflictException("Sensitive word already exists: " + newWord);
        }

        existing.setWord(newWord);
        return repository.save(existing);
    }

    @Override
    public void deleteWordByWord(String word) {
        boolean exists = repository.existsByWordIgnoreCase(word);
        if (!exists) {
            throw new NotFoundException("Word not found: " + word);
        }
        repository.deleteByWordIgnoreCase(word);
    }
}