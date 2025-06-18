package com.example.todo.controller;

import com.example.todo.model.TodoList;
import com.example.todo.service.TodoListService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    public ResponseEntity<TodoList> createTodoList(@Valid @RequestBody TodoList todoList) {
        // Set the mock user ID before saving
        todoList.setUserId("user123");

        TodoList createdList = todoListService.createTodoList(todoList);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdList);
    }

    /**
     * Deletes a specific Todo List by its ID.
     *
     * @param listId The ID of the Todo List to delete.
     * @return ResponseEntity indicating the result of the operation.
     */
    @DeleteMapping("/{listId}")
    public ResponseEntity<Void> deleteTodoList(@PathVariable String listId) {
        // Verify the list exists and belongs to the user (mock user)
        Optional<TodoList> listOpt = todoListService.getTodoListById(listId);

        if (listOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (!"user123".equals(listOpt.get().getUserId())) { // Mock user check
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        todoListService.deleteTodoList(listId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Updates a specific Todo List by its ID.
     *
     * @param listId The ID of the Todo List to update.
     * @param todoListRequest The updated Todo List data.
     * @return ResponseEntity containing the updated Todo List.
     */
    @PutMapping("/{listId}")
    public ResponseEntity<?> updateTodoList(@PathVariable String listId, @Valid @RequestBody TodoList todoListRequest) {
        if (todoListRequest.getId() != null && !listId.equals(todoListRequest.getId())) {
            Map<String, String> error = new HashMap<>();
            error.put("id", "ID in body must match ID in path, or be null.");
            return ResponseEntity.badRequest().body(error);
        }

        // Ownership check (using mock user for now)
        Optional<TodoList> existingListOpt = todoListService.getTodoListById(listId);
        if (existingListOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!"user123".equals(existingListOpt.get().getUserId())) { // Mock user check
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Ensure the request's userId is set to the existing one and ID is set to path ID
        todoListRequest.setUserId(existingListOpt.get().getUserId());
        todoListRequest.setId(listId); // Service uses listId param, but good to be consistent

        try {
            TodoList updatedList = todoListService.updateTodoList(listId, todoListRequest);
            return ResponseEntity.ok(updatedList);
        } catch (IllegalArgumentException e) {
            // Catches list not found from service, or task not found from service
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // Exception Handler for @Valid validation errors
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

    // General Exception Handler (Optional but good practice)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public Map<String, String> handleGeneralExceptions(Exception ex) {
        // TODO: Log the exception ex.printStackTrace(); or use a proper logger
        Map<String, String> error = new HashMap<>();
        error.put("error", "An unexpected error occurred");
        // Consider not exposing raw ex.getMessage() in production for some exceptions
        error.put("message", ex.getMessage());
        return error;
    }
}
