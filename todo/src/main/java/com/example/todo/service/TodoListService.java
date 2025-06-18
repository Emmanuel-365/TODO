package com.example.todo.service;

import com.example.todo.model.Task;
import com.example.todo.model.TodoList;
import com.example.todo.repository.TodoListRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        // Set creation timestamp for the list
        todoList.setCreatedAt(LocalDateTime.now());

        // Initialize tasks if null
        if (todoList.getTasks() == null) {
            todoList.setTasks(new ArrayList<>());
        }

        // Process each task
        for (Task task : todoList.getTasks()) {
            task.setCreatedAt(LocalDateTime.now());
            task.setTodoList(todoList); // Set the bidirectional relationship
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
     * @param listId The ID of the Todo List to update.
     * @param updatedList The Todo List with updated information.
     * @return The updated Todo List.
     */
    @Transactional
    public TodoList updateTodoList(String listId, TodoList updatedList) {
        TodoList existingList = todoListRepository.findById(listId)
                .orElseThrow(() -> new IllegalArgumentException("Todo List not found with ID: " + listId));

        // Update basic properties
        existingList.setTitle(updatedList.getTitle());

        // Create a map of existing tasks by ID for efficient lookup
        Map<String, Task> existingTasksMap = existingList.getTasks().stream()
                .collect(Collectors.toMap(Task::getId, Function.identity()));

        // Process updated tasks
        List<Task> updatedTasks = new ArrayList<>();
        for (Task updatedTask : updatedList.getTasks()) {
            Task task;
            if (updatedTask.getId() != null && existingTasksMap.containsKey(updatedTask.getId())) {
                // Update existing task
                task = existingTasksMap.get(updatedTask.getId());
                task.setText(updatedTask.getText());
                task.setDone(updatedTask.isDone());
            } else {
                // Create new task
                task = new Task();
                task.setText(updatedTask.getText());
                task.setDone(updatedTask.isDone());
                task.setCreatedAt(LocalDateTime.now());
            }
            task.setTodoList(existingList);
            updatedTasks.add(task);
        }

        // Update the list's tasks
        existingList.setTasks(updatedTasks);

        return todoListRepository.save(existingList);
    }
}

