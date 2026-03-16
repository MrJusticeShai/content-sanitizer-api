package com.flash.assessment.contentsanitizer.config;

import com.flash.assessment.contentsanitizer.entity.SensitiveWord;
import com.flash.assessment.contentsanitizer.repository.SensitiveWordRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Preloads SQL sensitive words into the database on application startup.
 */
@Component
@RequiredArgsConstructor
public class SensitiveWordPreloader implements CommandLineRunner {

    private final SensitiveWordRepository repository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        ClassPathResource resource = new ClassPathResource("sql_sensitive_list.txt");

        ObjectMapper mapper = new ObjectMapper();

        // Parse JSON array into list
        List<String> words = mapper.readValue(resource.getInputStream(), new TypeReference<List<String>>() {});

        // Insert only words that don't already exist
        words.forEach(word -> {
            if (!repository.existsByWordIgnoreCase(word)) {
                repository.save(SensitiveWord.builder()
                        .word(word.toUpperCase())
                        .build());
            }
        });

        System.out.println("SQL sensitive words preloaded: " + words.size());
    }
}
