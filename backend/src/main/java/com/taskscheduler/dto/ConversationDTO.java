package com.taskscheduler.dto;

import com.taskscheduler.entity.AiConversation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationDTO {
    private Long id;
    private Long userId;
    private Long taskId;
    private String userMessage;
    private String assistantResponse;
    private LocalDateTime createdAt;

    public static ConversationDTO fromEntity(AiConversation conversation) {
        return ConversationDTO.builder()
                .id(conversation.getId())
                .userId(conversation.getUser().getId())
                .taskId(conversation.getTask() != null ? conversation.getTask().getId() : null)
                .userMessage(conversation.getUserMessage())
                .assistantResponse(conversation.getAssistantResponse())
                .createdAt(conversation.getCreatedAt())
                .build();
    }
}
