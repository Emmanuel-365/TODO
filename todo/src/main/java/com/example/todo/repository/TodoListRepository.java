package com.example.todo.repository;

import com.example.todo.model.TodoList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoListRepository extends JpaRepository<TodoList, String> {
    
    /**
     * Find all todo lists belonging to a specific user.
     *
     * @param userId The ID of the user
     * @return List of todo lists for the user
     */
    List<TodoList> findByUserId(String userId);
}
