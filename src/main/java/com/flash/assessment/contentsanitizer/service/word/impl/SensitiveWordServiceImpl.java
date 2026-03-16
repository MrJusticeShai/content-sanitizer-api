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
        String newWord = updateRequest.getWord().trim();

        // Step 1: Find the entity we want to update
        SensitiveWord existing = repository.findByWordIgnoreCase(newWord)
                .orElseThrow(() -> new NotFoundException("Word not found: " + updateRequest.getWord()));

        // Step 2: If the new word is the same as the current word, do nothing
        if (existing.getWord().equalsIgnoreCase(newWord)) {
            return existing; // no changes needed
        }

        // Step 3: Check if the new word already exists elsewhere in the DB
        boolean duplicate = repository.existsByWordIgnoreCase(newWord);
        if (duplicate) {
            throw new ConflictException("Sensitive word already exists: " + newWord);
        }

        // Step 4: Update
        existing.setWord(newWord.toUpperCase());

        // Step 5: Save
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