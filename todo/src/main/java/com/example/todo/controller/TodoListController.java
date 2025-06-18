package com.example.todo.controller;

import com.example.todo.model.TodoList;
import com.example.todo.model.User;
import com.example.todo.service.TodoListService;
import com.example.todo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final UserService userService;

    public TodoListController(TodoListService todoListService, UserService userService) {
        this.todoListService = todoListService;
        this.userService = userService;
    }

    /**
     * Retrieves all Todo Lists for the authenticated user.
     *
     * @param principal The authenticated principal.
     * @return ResponseEntity containing the list of Todo Lists.
     */
    @GetMapping
    public ResponseEntity<List<TodoList>> getAllTodoLists(@AuthenticationPrincipal UserDetails principal) {
        User currentUser = userService.findUserByEmail(principal.getUsername());
        List<TodoList> todoLists = todoListService.getAllTodoListsByUserId(currentUser.getId());
        return ResponseEntity.ok(todoLists);
    }

    /**
     * Creates a new Todo List for the authenticated user.
     *
     * @param todoList The Todo List to create.
     * @param principal The authenticated principal.
     * @return ResponseEntity containing the created Todo List.
     */
    @PostMapping
    public ResponseEntity<TodoList> createTodoList(@Valid @RequestBody TodoList todoList, @AuthenticationPrincipal UserDetails principal) {
        User currentUser = userService.findUserByEmail(principal.getUsername());
        todoList.setUserId(currentUser.getId());

        TodoList createdList = todoListService.createTodoList(todoList);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdList);
    }

    /**
     * Deletes a specific Todo List by its ID.
     *
     * @param listId The ID of the Todo List to delete.
     * @param principal The authenticated principal.
     * @return ResponseEntity indicating the result of the operation.
     */
    @DeleteMapping("/{listId}")
    public ResponseEntity<Void> deleteTodoList(@PathVariable String listId, @AuthenticationPrincipal UserDetails principal) {
        User currentUser = userService.findUserByEmail(principal.getUsername());
        Optional<TodoList> listOpt = todoListService.getTodoListById(listId);

        if (listOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (!currentUser.getId().equals(listOpt.get().getUserId())) {
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
     * @param principal The authenticated principal.
     * @return ResponseEntity containing the updated Todo List.
     */
    @PutMapping("/{listId}")
    public ResponseEntity<?> updateTodoList(@PathVariable String listId,
                                            @Valid @RequestBody TodoList todoListRequest,
                                            @AuthenticationPrincipal UserDetails principal) {
        User currentUser = userService.findUserByEmail(principal.getUsername());

        if (todoListRequest.getId() != null && !listId.equals(todoListRequest.getId())) {
            Map<String, String> error = new HashMap<>();
            error.put("id", "ID in body must match ID in path, or be null.");
            return ResponseEntity.badRequest().body(error);
        }

        Optional<TodoList> existingListOpt = todoListService.getTodoListById(listId);
        if (existingListOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!currentUser.getId().equals(existingListOpt.get().getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Ensure the request's userId is set to the authenticated user's ID and ID is set to path ID
        todoListRequest.setUserId(currentUser.getId());
        todoListRequest.setId(listId);

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
