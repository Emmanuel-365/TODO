package com.example.todo.controller;

import com.example.todo.model.TodoList;
import com.example.todo.service.TodoListService;
import com.example.todo.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Controller for managing Todo Lists.
 * Provides endpoints for CRUD operations on Todo Lists.
 */
@RestController
@RequestMapping("/api/lists")
@CrossOrigin(origins = "*")
public class TodoListController {

    private final TodoListService todoListService;
    private final UserRepository userRepository;

    public TodoListController(TodoListService todoListService, UserRepository userRepository) {
        this.todoListService = todoListService;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves all Todo Lists for the authenticated user.
     *
     * @return ResponseEntity containing the list of Todo Lists.
     */
    @GetMapping
    public ResponseEntity<List<TodoList>> getAllTodoLists() {
        String userId = getCurrentUserId();
        List<TodoList> todoLists = todoListService.getAllTodoListsByUserId(userId);
        return ResponseEntity.ok(todoLists);
    }

    /**
     * Creates a new Todo List for the authenticated user.
     *
     * @param todoList The Todo List to create.
     * @return ResponseEntity containing the created Todo List.
     */
    @PostMapping
    public ResponseEntity<TodoList> createTodoList(@Valid @RequestBody TodoList todoList) {
        if (todoList.getTitle() == null || todoList.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        todoList.setUserId(getCurrentUserId());
        TodoList createdList = todoListService.createTodoList(todoList);
        return ResponseEntity.status(201).body(createdList);
    }

    /**
     * Deletes a specific Todo List by its ID.
     *
     * @param listId The ID of the Todo List to delete.
     * @return ResponseEntity indicating the result of the operation.
     */
    @DeleteMapping("/{listId}")
    public ResponseEntity<Void> deleteTodoList(@PathVariable String listId) {
        try {
            TodoList list = todoListService.getTodoListById(listId).orElse(null);
            if (list == null) {
                return ResponseEntity.notFound().build();
            }
            if (!getCurrentUserId().equals(list.getUserId())) {
                return ResponseEntity.status(403).build();
            }
            todoListService.deleteTodoList(listId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Updates a specific Todo List by its ID.
     *
     * @param listId The ID of the Todo List to update.
     * @param todoList The updated Todo List.
     * @return ResponseEntity containing the updated Todo List.
     */
    @PutMapping("/{listId}")
    public ResponseEntity<TodoList> updateTodoList(
            @PathVariable String listId,
            @Valid @RequestBody TodoList todoList) {
        if (!listId.equals(todoList.getId())) {
            return ResponseEntity.badRequest().build();
        }
        try {
            TodoList existingList = todoListService.getTodoListById(listId).orElse(null);
            if (existingList == null) {
                return ResponseEntity.notFound().build();
            }
            if (!getCurrentUserId().equals(existingList.getUserId())) {
                return ResponseEntity.status(403).build();
            }
            todoList.setUserId(existingList.getUserId());
            TodoList updatedList = todoListService.updateTodoList(listId, todoList);
            return ResponseEntity.ok(updatedList);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Méthode utilitaire pour récupérer l'id utilisateur depuis le contexte de sécurité
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            // Cherche l'utilisateur par email pour obtenir son UUID
            com.example.todo.model.User user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                return user.getId();
            }
        }
        return null;
    }
}
