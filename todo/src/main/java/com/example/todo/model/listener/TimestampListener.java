package main.java.com.example.todo.model.listener;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;

public class TimestampListener {

    @PrePersist
    public void setCreatedAt(Object entity) {
        if (entity instanceof TimestampedEntity) {
            ((TimestampedEntity) entity).setCreatedAt(LocalDateTime.now());
        }
    }
}

interface TimestampedEntity {
    void setCreatedAt(LocalDateTime dateTime);
}
