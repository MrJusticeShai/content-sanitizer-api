package com.flash.assessment.contentsanitizer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a sensitive word stored in the system.
 *
 * <p>
 * Sensitive words are used by the Content Sanitizer microservice to detect
 * and mask undesirable or restricted terms in user-generated messages.
 * </p>
 *
 * <p>
 * This entity supports CRUD operations to allow internal systems or administrators
 * to manage the list of sensitive words dynamically without redeploying the service.
 * </p>
 **/
@Entity
@Table(name = "sensitive_words",
        indexes = @Index(name = "idx_sensitive_word", columnList = "word")
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensitiveWord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String word;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
