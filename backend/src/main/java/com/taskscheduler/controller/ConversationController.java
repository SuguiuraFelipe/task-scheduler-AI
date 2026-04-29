package com.taskscheduler.controller;

import com.taskscheduler.dto.AITaskSuggestionRequest;
import com.taskscheduler.dto.AITaskSuggestion;
import com.taskscheduler.dto.ConversationDTO;
import com.taskscheduler.entity.User;
import com.taskscheduler.repository.AiConversationRepository;
import com.taskscheduler.repository.UserRepository;
import com.taskscheduler.service.ClaudeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@Slf4j
public class ConversationController {
    private final ClaudeService claudeService;
    private final AiConversationRepository conversationRepository;
    private final UserRepository userRepository;

    @PostMapping("/suggest-task")
    public ResponseEntity<AITaskSuggestion> suggestTask(
            @RequestBody AITaskSuggestionRequest request,
            Principal principal) {
        // Always use the authenticated user's email from the JWT — never trust client-provided userId
        String userEmail = principal.getName();
        log.info("Suggesting task for authenticated user: {}", userEmail);
        AITaskSuggestion suggestion = claudeService.suggestTask(request.getUserMessage(), userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(suggestion);
    }

    @GetMapping
    public ResponseEntity<List<ConversationDTO>> getConversations(Principal principal) {
        // Derive userId from the authenticated user, not from client request param
        String userEmail = principal.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + userEmail));

        List<ConversationDTO> conversations = conversationRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(ConversationDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<ConversationDTO>> getConversationsByTask(@PathVariable Long taskId) {
        List<ConversationDTO> conversations = conversationRepository.findByTaskIdOrderByCreatedAtDesc(taskId)
                .stream()
                .map(ConversationDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(conversations);
    }
}
