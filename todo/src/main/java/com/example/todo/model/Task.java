package com.example.todo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.example.todo.model.listener.TimestampListener;
import com.example.todo.model.listener.TimestampedEntity;

@Entity
@EntityListeners(TimestampListener.class)
public class Task implements TimestampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank(message = "Task text cannot be blank")
    private String text;

    private boolean done;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_list_id", nullable = false)
    @JsonIgnore  // Pour éviter la récursion infinie lors de la sérialisation JSON
    private TodoList todoList;

    // Constructeur par défaut
    public Task() {
        this.done = false; // Par défaut, une tâche n'est pas terminée
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        // Laisser vide pour empêcher la modification manuelle de l'id
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public TodoList getTodoList() {
        return todoList;
    }

    public void setTodoList(TodoList todoList) {
        this.todoList = todoList;
    }
}
