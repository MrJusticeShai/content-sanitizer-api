package com.flash.assessment.contentsanitizer.service.word.impl;

import com.flash.assessment.contentsanitizer.dto.CreateSensitiveWordRequest;
import com.flash.assessment.contentsanitizer.entity.SensitiveWord;
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
    public SensitiveWord createWord(CreateSensitiveWordRequest request) {
        SensitiveWord word = SensitiveWord.builder()
                .word(request.getWord().toUpperCase()) // store in uppercase for uniformity
                .build();
        return repository.save(word);
    }

    @Override
    public List<SensitiveWord> getAllWords() {
        return repository.findAll();
    }

    @Override
    public SensitiveWord updateWord(Long id, String word) {
        SensitiveWord existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Word not found"));
        existing.setWord(word.toUpperCase());
        return repository.save(existing);
    }

    @Override
    public void deleteWord(Long id) {
        repository.deleteById(id);
    }
}