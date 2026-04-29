package com.taskscheduler.service;

import com.taskscheduler.dto.CreateTaskRequest;
import com.taskscheduler.dto.TaskDTO;
import com.taskscheduler.dto.AITaskSuggestion;
import com.taskscheduler.entity.Task;
import com.taskscheduler.entity.User;
import com.taskscheduler.repository.AiConversationRepository;
import com.taskscheduler.repository.ReminderLogRepository;
import com.taskscheduler.repository.TaskRepository;
import com.taskscheduler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final AiConversationRepository aiConversationRepository;
    private final ReminderLogRepository reminderLogRepository;
    private final ClaudeService claudeService;
    private final EmailService emailService;

    @Transactional
    public TaskDTO createTask(CreateTaskRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + userEmail));

        Task task = new Task();
        task.setUser(user);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setStatus(Task.TaskStatus.PENDING);
        task.setPriority(Task.TaskPriority.valueOf(request.getPriority().toUpperCase()));

        Task saved = taskRepository.save(task);
        log.info("Tarefa criada com id: {} para utilizador: {}", saved.getId(), userEmail);
        log.info("A enviar email de confirmação da tarefa {} para {}", saved.getId(), user.getEmail());

        // Extract data while in transaction, then dispatch email using plain values
        emailService.sendTaskCreatedEmail(
                user.getEmail(), user.getName(),
                saved.getTitle(), saved.getDescription(),
                saved.getDueDate(), saved.getPriority()
        );

        return TaskDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksByUser(Long userId) {
        return taskRepository.findByUserIdOrderByDueDateAsc(userId).stream()
                .map(TaskDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskDTO getTaskById(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada: " + taskId));
        return TaskDTO.fromEntity(task);
    }

    @Transactional
    public TaskDTO updateTask(Long taskId, CreateTaskRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada: " + taskId));

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setPriority(Task.TaskPriority.valueOf(request.getPriority().toUpperCase()));
        task.setUpdatedAt(LocalDateTime.now());

        Task updated = taskRepository.save(task);
        log.info("Tarefa atualizada com id: {}", updated.getId());
        return TaskDTO.fromEntity(updated);
    }

    @Transactional
    public void deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada: " + taskId));

        reminderLogRepository.deleteByTaskId(taskId);
        aiConversationRepository.deleteByTaskId(taskId);
        taskRepository.delete(task);
        log.info("Tarefa deletada com id: {}", taskId);
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksDueBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return taskRepository.findTasksDueBetween(startDate, endDate);
    }

    @Transactional
    public void updateTaskStatus(Long taskId, Task.TaskStatus status) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada: " + taskId));
        task.setStatus(status);
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);
    }

    @Transactional
    public TaskDTO createTaskFromPrompt(String prompt, String userEmail) {
        AITaskSuggestion suggestion = claudeService.suggestTask(prompt, userEmail);

        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle(suggestion.getSuggestedTitle());
        request.setDescription(suggestion.getSuggestedDescription());
        request.setDueDate(suggestion.getSuggestedDueDate());
        request.setPriority(suggestion.getSuggestedPriority());

        return createTask(request, userEmail);
    }
}
