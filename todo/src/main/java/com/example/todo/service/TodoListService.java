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
    public TodoList updateTodoList(String listId, TodoList incomingTodoListData) {
        TodoList existingTodoList = todoListRepository.findById(listId)
                .orElseThrow(() -> new IllegalArgumentException("Todo List not found with ID: " + listId));

        // Update basic properties from incomingTodoListData
        existingTodoList.setTitle(incomingTodoListData.getTitle());
        // existingTodoList.setUserId(incomingTodoListData.getUserId()); // userId should be handled by auth context

        List<Task> tasksToKeepOrUpdate = new ArrayList<>();
        Map<String, Task> existingTasksMap = existingTodoList.getTasks().stream()
                .collect(Collectors.toMap(Task::getId, Function.identity()));

        if (incomingTodoListData.getTasks() != null) {
            for (Task incomingTask : incomingTodoListData.getTasks()) {
                if (incomingTask.getId() == null) { // New task
                    incomingTask.setCreatedAt(LocalDateTime.now());
                    // The new task must be associated with the TodoList entity for cascading persistence
                    tasksToKeepOrUpdate.add(incomingTask);
                } else { // Potential existing task
                    Task taskToUpdate = existingTasksMap.get(incomingTask.getId());
                    if (taskToUpdate != null) {
                        // Update existing task's properties
                        taskToUpdate.setText(incomingTask.getText());
                        taskToUpdate.setDone(incomingTask.isDone());
                        // taskToUpdate.setCreatedAt(taskToUpdate.getCreatedAt()); // createdAt is not changed
                        tasksToKeepOrUpdate.add(taskToUpdate);
                        existingTasksMap.remove(incomingTask.getId()); // Mark as processed
                    } else {
                        // Client sent a task with an ID, but it's not in the current list's tasks.
                        // This is an invalid state according to API doc ("Task IDs should be preserved if they exist")
                        throw new IllegalArgumentException("Task with ID " + incomingTask.getId() +
                                                           " provided for update does not exist in TodoList " + listId);
                    }
                }
            }
        }

        // existingTodoList.getTasks() is the managed collection.
        // Clear it and add back only the tasks that should be present.
        // Tasks that were in existingTasksMap but not added to tasksToKeepOrUpdate
        // (i.e., tasks that were in existingTasksMap but not in incomingTaskData.getTasks() with a matching ID)
        // will be removed by orphanRemoval=true.
        existingTodoList.getTasks().clear();
        existingTodoList.getTasks().addAll(tasksToKeepOrUpdate);

        // The existingTodoList.createdAt is not changed.
        return todoListRepository.save(existingTodoList);
    }
}

