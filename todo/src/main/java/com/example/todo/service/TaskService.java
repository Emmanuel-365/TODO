package com.example.todo.service;

import com.example.todo.model.Task;
import com.example.todo.model.TodoList;
import com.example.todo.repository.TaskRepository;
import com.example.todo.repository.TodoListRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TodoListRepository todoListRepository;

    public TaskService(TaskRepository taskRepository, TodoListRepository todoListRepository) {
        this.taskRepository = taskRepository;
        this.todoListRepository = todoListRepository;
    }

    /**
     * Vérifie si un utilisateur a accès à une tâche.
     *
     * @param taskId l'ID de la tâche
     * @param userId l'ID de l'utilisateur
     * @return true si l'utilisateur a accès à la tâche
     * @throws IllegalArgumentException si la tâche n'existe pas
     */
    public boolean hasAccessToTask(String taskId, String userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));
        return task.getTodoList().getUserId().equals(userId);
    }

    /**
     * Crée une nouvelle tâche dans une liste donnée.
     *
     * @param listId l'ID de la liste
     * @param task la tâche à créer
     * @return la tâche créée
     */
    @Transactional
    public Task createTask(String listId, Task task) {
        TodoList todoList = todoListRepository.findById(listId)
                .orElseThrow(() -> new IllegalArgumentException("TodoList not found with ID: " + listId));

        task.setCreatedAt(LocalDateTime.now());
        task.setTodoList(todoList);
        Task savedTask = taskRepository.save(task);
        
        todoList.addTask(savedTask);
        todoListRepository.save(todoList);
        
        return savedTask;
    }

    /**
     * Met à jour une tâche existante.
     *
     * @param taskId l'ID de la tâche
     * @param updatedTask la tâche avec les nouvelles données
     * @return la tâche mise à jour
     */
    @Transactional
    public Task updateTask(String taskId, Task updatedTask) {
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));
        // Vérifie que la tâche appartient à la même liste
        // if (!existingTask.getTodoList().getId().equals(updatedTask.getTodoList().getId())) {
        //     throw new IllegalArgumentException("Cannot change the TodoList of a task");
        // }

        // Met à jour les champs modifiables
        existingTask.setText(updatedTask.getText());
        existingTask.setDone(updatedTask.isDone());

        return taskRepository.save(existingTask);
    }

    /**
     * Change l'état (done/not done) d'une tâche.
     *
     * @param taskId l'ID de la tâche
     * @param done le nouvel état
     * @return la tâche mise à jour
     */
    @Transactional
    public Task toggleTaskStatus(String taskId, boolean done) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));
        
        task.setDone(done);
        return taskRepository.save(task);
    }

    /**
     * Supprime une tâche.
     *
     * @param taskId l'ID de la tâche à supprimer
     */
    @Transactional
    public void deleteTask(String taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        TodoList todoList = task.getTodoList();
        todoList.removeTask(task);
        todoListRepository.save(todoList);
        
        taskRepository.delete(task);
    }

    /**
     * Récupère une tâche par son ID.
     *
     * @param taskId l'ID de la tâche
     * @return la tâche si elle existe
     */
    public Optional<Task> getTaskById(String taskId) {
        return taskRepository.findById(taskId);
    }
}
