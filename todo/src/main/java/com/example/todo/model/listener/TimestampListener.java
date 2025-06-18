package com.example.todo.model.listener;

import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;

/**
 * Listener JPA qui gère automatiquement les timestamps de création pour les entités.
 */
public class TimestampListener {

    @PrePersist
    public void setCreatedAt(Object entity) {
        if (entity instanceof TimestampedEntity) {
            ((TimestampedEntity) entity).setCreatedAt(LocalDateTime.now());
        }
    }
}
