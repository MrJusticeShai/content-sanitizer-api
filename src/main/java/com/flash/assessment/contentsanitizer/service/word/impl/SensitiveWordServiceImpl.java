package com.flash.assessment.contentsanitizer.service.word.impl;

import com.flash.assessment.contentsanitizer.dto.CreateSensitiveWordRequest;
import com.flash.assessment.contentsanitizer.dto.UpdateSensitiveWordRequest;
import com.flash.assessment.contentsanitizer.entity.SensitiveWord;
import com.flash.assessment.contentsanitizer.exception.BadRequestException;
import com.flash.assessment.contentsanitizer.exception.ConflictException;
import com.flash.assessment.contentsanitizer.exception.NotFoundException;
import com.flash.assessment.contentsanitizer.repository.SensitiveWordRepository;
import com.flash.assessment.contentsanitizer.service.word.SensitiveWordService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class SensitiveWordServiceImpl implements SensitiveWordService {

    private final SensitiveWordRepository repository;
    private final AtomicReference<List<Pattern>> cachedPatterns = new AtomicReference<>(List.of());
    @Value("${sanitizer.bulk.max-size}")
    private int bulkMaxSize;

    @PostConstruct
    public void refreshCache() {
        try {
            List<Pattern> patterns = getAllWords().stream()
                    .map(SensitiveWord::getWord)
                    .filter(StringUtils::hasText)
                    .map(word -> Pattern.compile("(?i)\\b" + Pattern.quote(word) + "\\b"))
                    .toList();

            cachedPatterns.set(patterns);
            log.info("Sanitization cache refreshed — {} pattern(s) loaded.", patterns.size());
        } catch (Exception e) {
            log.error("Cache refresh failed — retaining existing {} pattern(s).", cachedPatterns.get().size(), e);
        }
    }

    @Override
    public List<Pattern> getCachedPatterns() {
        return cachedPatterns.get();
    }

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
        refreshCache();

        return saved;
    }

    @Override
    public List<SensitiveWord> bulkCreateWords(List<CreateSensitiveWordRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new BadRequestException("Word list must not be empty");
        }

        if (requests.size() > bulkMaxSize) {
            throw new BadRequestException(
                    "Bulk request exceeds maximum allowed size of " + bulkMaxSize + " words");
        }

        // DB call 1 — fetch all existing words once, reused for duplicate filtering
        Set<String> existingWordSet = repository.findAll().stream()
                .map(w -> w.getWord().toUpperCase())
                .collect(Collectors.toSet());

        List<SensitiveWord> toSave = requests.stream()
                .map(r -> r.getWord().trim().toUpperCase())
                .filter(StringUtils::hasText)
                .distinct()
                .filter(word -> !existingWordSet.contains(word))
                .map(word -> SensitiveWord.builder().word(word).build())
                .toList();

        // DB call 2 — persist all new words in a single transaction
        List<SensitiveWord> saved = repository.saveAll(toSave);

        // DB call 3 — refreshCache() owns cache compilation
        refreshCache();

        log.info("Bulk create — {} word(s) saved, {} skipped.",
                saved.size(), requests.size() - saved.size());

        return saved;
    }

    @Transactional(readOnly = true)
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
        refreshCache();
        return repository.save(existing);
    }

    @Override
    public void deleteWordByWord(String word) {
        boolean exists = repository.existsByWordIgnoreCase(word);
        if (!exists) {
            throw new NotFoundException("Word not found: " + word);
        }

        repository.deleteByWordIgnoreCase(word);
        refreshCache();
    }
}