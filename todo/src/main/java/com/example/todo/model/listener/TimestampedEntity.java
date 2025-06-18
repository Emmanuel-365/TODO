package com.example.todo.model.listener;

import java.time.LocalDateTime;

/**
 * Interface pour les entités qui ont besoin d'un timestamp de création.
 * Les entités implémentant cette interface seront automatiquement gérées par TimestampListener.
 */
public interface TimestampedEntity {
    void setCreatedAt(LocalDateTime dateTime);
}
