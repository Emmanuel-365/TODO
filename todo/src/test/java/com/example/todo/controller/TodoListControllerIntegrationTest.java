package com.example.todo.controller;

import com.example.todo.model.Task;
import com.example.todo.model.TodoList;
import com.example.todo.repository.TodoListRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional; // Important for tests that modify data

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Ensures tests are rolled back, keeping test environment clean
class TodoListControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TodoListRepository todoListRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final String mockUserId = "user123"; // Consistent with controller's temporary mock user

    @BeforeEach
    void setUp() {
        // Clean up database before each test if @Transactional is not aggressive enough for all scenarios
        // or if we weren't using @Transactional at class level.
        // For @Transactional, this might be redundant but safe.
        todoListRepository.deleteAll();
    }

    private TodoList createAndSaveTestList(String title, String userId, int taskCount) {
        TodoList list = new TodoList();
        list.setTitle(title);
        list.setUserId(userId);
        list.setCreatedAt(LocalDateTime.now());
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < taskCount; i++) {
            Task task = new Task();
            task.setText("Test Task " + (i + 1) + " for " + title);
            task.setDone(false);
            task.setCreatedAt(LocalDateTime.now().plusSeconds(i));
            // task.setTodoList(list); // JPA relationship handling
            tasks.add(task);
        }
        list.setTasks(tasks);
        return todoListRepository.save(list);
    }

    @Test
    void getAllTodoLists_whenNoLists_returnsEmptyArray() throws Exception {
        mockMvc.perform(get("/api/lists")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getAllTodoLists_whenListsExist_returnsListsForMockUser() throws Exception {
        createAndSaveTestList("List 1", mockUserId, 1);
        createAndSaveTestList("List 2", mockUserId, 0);
        createAndSaveTestList("List for other user", "otheruser999", 1); // Should not be returned

        mockMvc.perform(get("/api/lists")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("List 1")))
                .andExpect(jsonPath("$[1].title", is("List 2")));
    }

    @Test
    void createTodoList_withValidTitle_returnsCreatedList() throws Exception {
        TodoList newList = new TodoList();
        newList.setTitle("New Test List");
        // UserId will be set by controller

        mockMvc.perform(post("/api/lists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newList)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is("New Test List")))
                .andExpect(jsonPath("$.userId", is(mockUserId)))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.tasks", hasSize(0)));

        assertEquals(1, todoListRepository.findByUserId(mockUserId).size());
    }

    @Test
    void createTodoList_withBlankTitle_returnsBadRequest() throws Exception {
        TodoList newList = new TodoList();
        newList.setTitle(""); // Blank title

        mockMvc.perform(post("/api/lists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newList)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("TodoList title cannot be blank")));
    }

    @Test
    void createTodoList_withTasks_returnsCreatedListWithTasks() throws Exception {
        TodoList newList = new TodoList();
        newList.setTitle("List with Tasks");
        Task task1 = new Task();
        task1.setText("Task 1 for new list");
        // task1.setDone(false); // Default
        newList.setTasks(List.of(task1));

        mockMvc.perform(post("/api/lists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newList)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("List with Tasks")))
                .andExpect(jsonPath("$.tasks", hasSize(1)))
                .andExpect(jsonPath("$.tasks[0].id", notNullValue()))
                .andExpect(jsonPath("$.tasks[0].text", is("Task 1 for new list")))
                .andExpect(jsonPath("$.tasks[0].createdAt", notNullValue()));
    }


    @Test
    void deleteTodoList_existingListAndOwner_returnsNoContent() throws Exception {
        TodoList list = createAndSaveTestList("To Be Deleted", mockUserId, 0);

        mockMvc.perform(delete("/api/lists/" + list.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        assertFalse(todoListRepository.findById(list.getId()).isPresent());
    }

    @Test
    void deleteTodoList_nonExistentList_returnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/lists/nonexistent-id-123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTodoList_existingListButNotOwner_returnsForbidden() throws Exception {
        TodoList list = createAndSaveTestList("Someone Else's List", "anotherUser", 0);

        mockMvc.perform(delete("/api/lists/" + list.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        assertTrue(todoListRepository.findById(list.getId()).isPresent()); // Still exists
    }

    @Test
    void updateTodoList_updateTitle_returnsOkWithUpdatedList() throws Exception {
        TodoList originalList = createAndSaveTestList("Original Title", mockUserId, 1);

        TodoList updatedRequest = new TodoList();
        // updatedRequest.setId(originalList.getId()); // Path ID must match body ID if present - or ID can be omitted from body
        updatedRequest.setTitle("Updated Title");

        // Create a new list for tasks to avoid modifying originalList's task list directly
        List<Task> tasksForRequest = new ArrayList<>();
        for(Task task : originalList.getTasks()){
            Task newTask = new Task();
            newTask.setId(task.getId());
            newTask.setText(task.getText());
            newTask.setDone(task.isDone());
            newTask.setCreatedAt(task.getCreatedAt());
            tasksForRequest.add(newTask);
        }
        updatedRequest.setTasks(tasksForRequest);


        mockMvc.perform(put("/api/lists/" + originalList.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(originalList.getId())))
                .andExpect(jsonPath("$.title", is("Updated Title")))
                .andExpect(jsonPath("$.userId", is(mockUserId)))
                .andExpect(jsonPath("$.createdAt", is(originalList.getCreatedAt().toString())))
                .andExpect(jsonPath("$.tasks", hasSize(1)));

        TodoList fetchedList = todoListRepository.findById(originalList.getId()).get();
        assertEquals("Updated Title", fetchedList.getTitle());
    }

    @Test
    void updateTodoList_nonExistentList_returnsNotFound() throws Exception {
        TodoList updatedRequest = new TodoList();
        // updatedRequest.setId("nonexistent-list-id"); // ID in body is optional if it matches path
        updatedRequest.setTitle("Title for non-existent list");

        mockMvc.perform(put("/api/lists/nonexistent-list-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTodoList_listIdInPathAndBodyMismatch_returnsBadRequest() throws Exception {
        TodoList list = createAndSaveTestList("Mismatched List", mockUserId, 0);
        TodoList updatedRequest = new TodoList();
        updatedRequest.setId("some-other-id-in-body"); // Different ID
        updatedRequest.setTitle("Updated Title");

        mockMvc.perform(put("/api/lists/" + list.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedRequest)))
                .andExpect(status().isBadRequest())
                // $.id was the key in controller's error map
                .andExpect(jsonPath("$.id", is("ID in body must match ID in path, or be null.")));
    }


    @Test
    void updateTodoList_blankTitle_returnsBadRequest() throws Exception {
        TodoList originalList = createAndSaveTestList("Valid Title List", mockUserId, 0);
        TodoList updatedRequest = new TodoList();
        // updatedRequest.setId(originalList.getId()); // ID in body is optional
        updatedRequest.setTitle(""); // Invalid blank title
        updatedRequest.setTasks(new ArrayList<>());


        mockMvc.perform(put("/api/lists/" + originalList.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("TodoList title cannot be blank")));
    }

    @Test
    void updateTodoList_addUpdateRemoveTasks_returnsOkWithModifiedTasks() throws Exception {
        TodoList list = new TodoList();
        list.setTitle("Task Management List");
        list.setUserId(mockUserId);
        // list.setCreatedAt(LocalDateTime.now()); // Will be set by service on creation/save

        Task task1_orig = new Task();
        task1_orig.setText("Task 1 Original");
        task1_orig.setDone(false);
        // task1_orig.setCreatedAt(LocalDateTime.now().minusHours(1)); // Will be set by service

        Task task2_orig_to_be_removed = new Task();
        task2_orig_to_be_removed.setText("Task 2 To Be Removed");
        task2_orig_to_be_removed.setDone(false);
        // task2_orig_to_be_removed.setCreatedAt(LocalDateTime.now().minusMinutes(30)); // Will be set

        list.setTasks(new ArrayList<>(List.of(task1_orig, task2_orig_to_be_removed)));
        // Save through service/API to get createdAt, task IDs, etc. properly initialized.
        // For this test, saving directly to repo is okay if we manage IDs carefully.
        // Let's save directly and then retrieve to get IDs.
        TodoList savedListInitial = todoListRepository.save(list);
        String savedListId = savedListInitial.getId();
        // Retrieve again to ensure we have IDs generated by persistence layer
        TodoList savedList = todoListRepository.findById(savedListId).orElseThrow();


        String task1Id = savedList.getTasks().stream().filter(t -> t.getText().equals("Task 1 Original")).findFirst().get().getId();
        String task2Id = savedList.getTasks().stream().filter(t -> t.getText().equals("Task 2 To Be Removed")).findFirst().get().getId();
        LocalDateTime task1OrigCreatedAt = savedList.getTasks().stream().filter(t -> t.getId().equals(task1Id)).findFirst().get().getCreatedAt();


        // Prepare update request
        TodoList updateRequest = new TodoList();
        // updateRequest.setId(savedListId); // ID in body is optional for PUT
        updateRequest.setTitle("Task Management List Updated");

        // Task 1: Update existing task
        Task task1_updated = new Task();
        task1_updated.setId(task1Id);
        task1_updated.setText("Task 1 Updated Text");
        task1_updated.setDone(true);
        // Client should not send createdAt for existing tasks if it's not meant to be updated.
        // The service logic ensures original createdAt is preserved for existing tasks.

        // Task 3: New task (no ID)
        Task task3_new = new Task();
        task3_new.setText("Task 3 New");
        task3_new.setDone(false);

        updateRequest.setTasks(List.of(task1_updated, task3_new));

        mockMvc.perform(put("/api/lists/" + savedListId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedListId)))
                .andExpect(jsonPath("$.tasks", hasSize(2)))
                .andExpect(jsonPath("$.tasks[?(@.text == 'Task 1 Updated Text')].done", contains(true)))
                .andExpect(jsonPath("$.tasks[?(@.text == 'Task 3 New')].id", notNullValue()));

        TodoList fetchedList = todoListRepository.findById(savedListId).orElseThrow();
        assertEquals(2, fetchedList.getTasks().size());

        Task fetchedTask1 = fetchedList.getTasks().stream().filter(t -> t.getId().equals(task1Id)).findFirst().orElse(null);
        assertNotNull(fetchedTask1);
        assertEquals("Task 1 Updated Text", fetchedTask1.getText());
        assertTrue(fetchedTask1.isDone());
        // Assert that original createdAt for task1 is preserved
        assertNotNull(fetchedTask1.getCreatedAt());
        assertEquals(task1OrigCreatedAt.withNano(0), fetchedTask1.getCreatedAt().withNano(0));


        Task fetchedTask3 = fetchedList.getTasks().stream().filter(t -> t.getText().equals("Task 3 New")).findFirst().orElse(null);
        assertNotNull(fetchedTask3);
        assertNotNull(fetchedTask3.getId());
        assertFalse(fetchedTask3.isDone());
        assertNotNull(fetchedTask3.getCreatedAt());

        String finalTask2Id = task2Id;
        assertTrue(fetchedList.getTasks().stream().noneMatch(t -> t.getId().equals(finalTask2Id)));
    }

    @Test
    void updateTodoList_updateTaskWithInvalidId_returnsNotFound() throws Exception {
        TodoList list = createAndSaveTestList("List for Invalid Task Update", mockUserId, 1);
        String validTaskId = list.getTasks().get(0).getId();
        Task existingValidTask = list.getTasks().get(0);

        TodoList updateRequest = new TodoList();
        // updateRequest.setId(list.getId()); // Optional in body
        updateRequest.setTitle(list.getTitle());

        Task invalidTaskUpdate = new Task();
        invalidTaskUpdate.setId("non-existent-task-id-999");
        invalidTaskUpdate.setText("Trying to update non-existent task");
        invalidTaskUpdate.setDone(true);

        Task validTaskForRequest = new Task();
        validTaskForRequest.setId(validTaskId);
        validTaskForRequest.setText(existingValidTask.getText());
        validTaskForRequest.setDone(existingValidTask.isDone());
        // validTaskForRequest.setCreatedAt(existingValidTask.getCreatedAt()); // Not needed from client for existing


        updateRequest.setTasks(List.of(validTaskForRequest, invalidTaskUpdate));

        mockMvc.perform(put("/api/lists/" + list.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("Task with ID non-existent-task-id-999 provided for update does not exist in TodoList " + list.getId())));
    }
}
