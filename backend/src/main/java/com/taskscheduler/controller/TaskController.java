package com.taskscheduler.controller;

import com.taskscheduler.dto.CreateTaskRequest;
import com.taskscheduler.dto.CreateTaskFromPromptRequest;
import com.taskscheduler.dto.TaskDTO;
import com.taskscheduler.entity.User;
import com.taskscheduler.repository.UserRepository;
import com.taskscheduler.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {
    private final TaskService taskService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<TaskDTO>> getTasks(Principal principal) {
        Long userId = resolveUserId(principal);
        List<TaskDTO> tasks = taskService.getTasksByUser(userId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTask(@PathVariable Long id) {
        TaskDTO task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    @PostMapping
    public ResponseEntity<TaskDTO> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            Principal principal) {
        String userEmail = principal.getName();
        log.info("Creating task for authenticated user: {}", userEmail);
        TaskDTO task = taskService.createTask(request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody CreateTaskRequest request) {
        TaskDTO task = taskService.updateTask(id, request);
        return ResponseEntity.ok(task);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/from-prompt")
    public ResponseEntity<TaskDTO> createTaskFromPrompt(
            @Valid @RequestBody CreateTaskFromPromptRequest request,
            Principal principal) {
        String userEmail = principal.getName();
        TaskDTO task = taskService.createTaskFromPrompt(request.getPrompt(), userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    private Long resolveUserId(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + principal.getName()));
    }
}
