package com.example.todo.controller;

import com.example.todo.model.Task;
import com.example.todo.model.User;
import com.example.todo.repository.UserRepository;
import com.example.todo.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/lists/{listId}/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);
    private final TaskService taskService;
    private final UserRepository userRepository;

    public TaskController(TaskService taskService, UserRepository userRepository) {
        this.taskService = taskService;
        this.userRepository = userRepository;
    }

    // Méthode utilitaire pour récupérer l'id utilisateur depuis le contexte de sécurité
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            // Cherche l'utilisateur par email pour obtenir son UUID
            User user = this.userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                return user.getId();
            }
        }
        return null;
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
    // copilot, debug chaque ligne de cette fonction
    @PutMapping("/{taskId}")
    public ResponseEntity<Task> updateTask(
            @PathVariable String listId,
            @PathVariable String taskId,
            @Valid @RequestBody Task task) {
        logger.info("Début de la mise à jour de la tâche taskId={}, listId={}", taskId, listId);
        try {
            // Vérification de l'existence de la tâche
            logger.debug("Recherche de la tâche avec taskId={}", taskId);
            Task existingTask = taskService.getTaskById(taskId).orElse(null);
            if (existingTask == null) {
                logger.warn("Tâche avec taskId={} non trouvée", taskId);
                return ResponseEntity.notFound().build();
            }
            logger.debug("Tâche trouvée : {}", existingTask);

            // Vérification que la tâche appartient à la liste spécifiée
            logger.debug("Vérification que la tâche appartient à la liste listId={}", listId);
            if (!existingTask.getTodoList().getId().equals(listId)) {
                logger.warn("La tâche taskId={} n'appartient pas à la liste listId={}", taskId, listId);
                return ResponseEntity.notFound().build();
            }

            // Vérification de l'autorisation de l'utilisateur
            String currentUserId = getCurrentUserId();
            String taskUserId = existingTask.getTodoList().getUserId();
            logger.debug("Vérification des autorisations : currentUserId={}, taskUserId={}", currentUserId, taskUserId);
            if (currentUserId == null) {
                logger.error("Aucun utilisateur authentifié trouvé");
                return ResponseEntity.status(401).build(); // Unauthorized si pas d'utilisateur
            }
            if (!taskUserId.equals(currentUserId)) {
                logger.warn("Accès interdit : currentUserId={} ne correspond pas à taskUserId={}", currentUserId, taskUserId);
                return ResponseEntity.status(403).build();
            }

            // Mise à jour de la tâche
            logger.debug("Mise à jour de la tâche taskId={}", taskId);
            Task updatedTask = taskService.updateTask(taskId, task);
            logger.info("Tâche taskId={} mise à jour avec succès", taskId);
            return ResponseEntity.ok(updatedTask);
        } catch (IllegalArgumentException e) {
            logger.error("Erreur de validation lors de la mise à jour de la tâche taskId={} : {}", taskId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Erreur inattendue lors de la mise à jour de la tâche taskId={} : {}", taskId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
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
