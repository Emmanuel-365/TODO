package com.example.todo.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.example.todo.model.listener.TimestampListener;
import com.example.todo.model.listener.TimestampedEntity;

@Entity
@EntityListeners(TimestampListener.class)
public class TodoList implements TimestampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank(message = "TodoList title cannot be blank")
    private String title;

    @OneToMany(
        mappedBy = "todoList",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Valid
    private List<Task> tasks = new ArrayList<>();

    @Column(updatable = false)
    private LocalDateTime createdAt;

    // @NotNull(message = "User ID cannot be null")
    private String userId;

    // Constructeur par défaut
    public TodoList() {
        this.tasks = new ArrayList<>();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        // Laisser vide pour empêcher la modification manuelle de l'id
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks.clear();
        if (tasks != null) {
            tasks.forEach(this::addTask);
        }
    }

    // Helper methods to maintain the bidirectional relationship
    public void addTask(Task task) {
        tasks.add(task);
        task.setTodoList(this);
    }

    public void removeTask(Task task) {
        tasks.remove(task);
        task.setTodoList(null);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
