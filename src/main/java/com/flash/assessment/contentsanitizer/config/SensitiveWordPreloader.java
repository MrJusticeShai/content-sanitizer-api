package com.flash.assessment.contentsanitizer.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flash.assessment.contentsanitizer.dto.CreateSensitiveWordRequest;
import com.flash.assessment.contentsanitizer.service.word.SensitiveWordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Preloads SQL sensitive words into the database on application startup.
 *
 * <p>
 * Reads words from {@code sql_sensitive_list.txt} on the classpath and
 * inserts them in batches using {@link SensitiveWordService#bulkCreateWords}.
 * Words that already exist are skipped silently.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SensitiveWordPreloader implements CommandLineRunner {

    private final SensitiveWordService wordService;

    @Value("${sanitizer.bulk.max-size}")
    private int bulkMaxSize;

    @Override
    public void run(String... args) throws Exception {
        ClassPathResource resource = new ClassPathResource("sql_sensitive_list.txt");
        ObjectMapper mapper = new ObjectMapper();

        List<String> words = mapper.readValue(
                resource.getInputStream(),
                new TypeReference<List<String>>() {}
        );

        // Partition into batches of bulkMaxSize to respect the configured limit
        List<CreateSensitiveWordRequest> requests = words.stream()
                .map(word -> {
                    CreateSensitiveWordRequest req = new CreateSensitiveWordRequest();
                    req.setWord(word);
                    return req;
                })
                .toList();

        int total = 0;
        for (int i = 0; i < requests.size(); i += bulkMaxSize) {
            List<CreateSensitiveWordRequest> batch = requests.subList(
                    i, Math.min(i + bulkMaxSize, requests.size())
            );
            int saved = wordService.bulkCreateWords(batch).size();
            total += saved;
            log.info("Preloader — batch {}/{} processed, {} word(s) saved.",
                    (i / bulkMaxSize) + 1,
                    (int) Math.ceil((double) requests.size() / bulkMaxSize),
                    saved);
        }

        log.info("Preloader complete — {} new word(s) inserted from {} total.",
                total, words.size());
    }
}