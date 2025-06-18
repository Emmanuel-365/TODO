package com.example.todo.controller;

import com.example.todo.model.Task;
import com.example.todo.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/lists/{listId}/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Crée une nouvelle tâche dans une liste.
     */
    @PostMapping
    public ResponseEntity<Task> createTask(
            @PathVariable String listId,
            @Valid @RequestBody Task task) {
        try {
            Task createdTask = taskService.createTask(listId, task);
            return ResponseEntity.status(201).body(createdTask);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Met à jour une tâche existante.
     */
    @PutMapping("/{taskId}")
    public ResponseEntity<Task> updateTask(
            @PathVariable String listId,
            @PathVariable String taskId,
            @Valid @RequestBody Task task) {
        try {
            // Vérifie que la tâche existe et appartient à la bonne liste
            Task existingTask = taskService.getTaskById(taskId)
                    .orElse(null);
            
            if (existingTask == null || !existingTask.getTodoList().getId().equals(listId)) {
                return ResponseEntity.notFound().build();
            }

            Task updatedTask = taskService.updateTask(taskId, task);
            return ResponseEntity.ok(updatedTask);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Change l'état d'une tâche (terminée/non terminée).
     */
    @PatchMapping("/{taskId}/toggle")
    public ResponseEntity<Task> toggleTaskStatus(
            @PathVariable String listId,
            @PathVariable String taskId,
            @RequestBody boolean done) {
        try {
            // Vérifie que la tâche existe et appartient à la bonne liste
            Task existingTask = taskService.getTaskById(taskId)
                    .orElse(null);
            
            if (existingTask == null || !existingTask.getTodoList().getId().equals(listId)) {
                return ResponseEntity.notFound().build();
            }

            Task updatedTask = taskService.toggleTaskStatus(taskId, done);
            return ResponseEntity.ok(updatedTask);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Supprime une tâche.
     */
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable String listId,
            @PathVariable String taskId) {
        try {
            // Vérifie que la tâche existe et appartient à la bonne liste
            Task existingTask = taskService.getTaskById(taskId)
                    .orElse(null);
            
            if (existingTask == null || !existingTask.getTodoList().getId().equals(listId)) {
                return ResponseEntity.notFound().build();
            }

            taskService.deleteTask(taskId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
