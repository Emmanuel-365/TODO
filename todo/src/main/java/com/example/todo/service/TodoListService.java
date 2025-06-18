package com.example.todo.service;

import com.example.todo.model.Task;
import com.example.todo.model.TodoList;
import com.example.todo.repository.TodoListRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing Todo Lists.
 * Provides methods to create, retrieve, update, and delete Todo Lists.
 */
@Service
public class TodoListService {

    private final TodoListRepository todoListRepository;

    public TodoListService(TodoListRepository todoListRepository) {
        this.todoListRepository = todoListRepository;
    }

    /**
     * Retrieves all Todo Lists for a specific user.
     *
     * @param userId The ID of the user
     * @return List of all Todo Lists for the user.
     */
    public List<TodoList> getAllTodoListsByUserId(String userId) {
        return todoListRepository.findByUserId(userId);
    }

    /**
     * Retrieves a Todo List by its ID.
     *
     * @param id The ID of the Todo List.
     * @return Optional containing the Todo List if found, or empty if not found.
     */
    public Optional<TodoList> getTodoListById(String id) {
        return todoListRepository.findById(id);
    }

    /**
     * Creates a new Todo List.
     *
     * @param todoList The Todo List to create.
     * @return The created Todo List.
     */
    @Transactional
    public TodoList createTodoList(TodoList todoList) {
        // Initialize collections if null
        if (todoList.getTasks() == null) {
            todoList.setTasks(new ArrayList<>());
        }

        // Set creation timestamp for the list
        todoList.setCreatedAt(LocalDateTime.now());

        // Set creation timestamp for any tasks
        for (Task task : todoList.getTasks()) {
            if (task.getCreatedAt() == null) {
                task.setCreatedAt(LocalDateTime.now());
            }
        }

        return todoListRepository.save(todoList);
    }

    /**
     * Deletes a Todo List by its ID.
     *
     * @param id The ID of the Todo List to delete.
     */
    @Transactional
    public void deleteTodoList(String id) {
        todoListRepository.deleteById(id);
    }

    /**
     * Updates an existing Todo List.
     *
     * @param todoList The Todo List with updated information.
     * @return The updated Todo List.
     */    @Transactional
    public TodoList updateTodoList(TodoList todoList) {
        if (!todoListRepository.existsById(todoList.getId())) {
            throw new IllegalArgumentException("Todo List not found");
        }

        for (Task task : todoList.getTasks()) {
            if (task.getCreatedAt() == null) {
                task.setCreatedAt(LocalDateTime.now());
            }
        }

        return todoListRepository.save(todoList);
    }
}

