package com.example.todo.controller;

import com.example.todo.model.TodoList;
import com.example.todo.service.TodoListService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing Todo Lists.
 * Provides endpoints for CRUD operations on Todo Lists.
 */
@RestController
@RequestMapping("/api/lists")
@CrossOrigin(origins = "*") // Enable CORS for frontend development
public class TodoListController {

    private final TodoListService todoListService;

    public TodoListController(TodoListService todoListService) {
        this.todoListService = todoListService;
    }

    /**
     * Retrieves all Todo Lists for the authenticated user.
     *
     * @return ResponseEntity containing the list of Todo Lists.
     */
    @GetMapping
    public ResponseEntity<List<TodoList>> getAllTodoLists() {
        // Note: In a real application, we would get the userId from the security context
        // For now, we're using a mock user ID
        String userId = "user123"; // Mock user ID
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
    public ResponseEntity<TodoList> createTodoList(@RequestBody TodoList todoList) {
        // Set the mock user ID
        todoList.setUserId("user123");
        
        // Validate the request
        if (todoList.getTitle() == null || todoList.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

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
            // Verify the list exists and belongs to the user
            TodoList list = todoListService.getTodoListById(listId)
                .orElse(null);
            
            if (list == null) {
                return ResponseEntity.notFound().build();
            }
            
            // In a real application, we would check if the list belongs to the authenticated user
            if (!"user123".equals(list.getUserId())) {
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
    public ResponseEntity<TodoList> updateTodoList(@PathVariable String listId, @RequestBody TodoList todoList) {
        // Verify the path ID matches the body ID
        if (!listId.equals(todoList.getId())) {
            return ResponseEntity.badRequest().build();
        }

        try {
            // Verify the list exists and belongs to the user
            TodoList existingList = todoListService.getTodoListById(listId)
                .orElse(null);
            
            if (existingList == null) {
                return ResponseEntity.notFound().build();
            }
            
            // In a real application, we would check if the list belongs to the authenticated user
            if (!"user123".equals(existingList.getUserId())) {
                return ResponseEntity.status(403).build();
            }

            // Preserve the original creation date and user ID
            todoList.setCreatedAt(existingList.getCreatedAt());
            todoList.setUserId(existingList.getUserId());
            
            // Update the list
            TodoList updatedList = todoListService.updateTodoList(todoList);
            return ResponseEntity.ok(updatedList);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
