package com.taskscheduler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AITaskSuggestion {
    private String suggestedTitle;
    private String suggestedDescription;
    private LocalDateTime suggestedDueDate;
    private String suggestedPriority;
    private String rawResponse;
}
