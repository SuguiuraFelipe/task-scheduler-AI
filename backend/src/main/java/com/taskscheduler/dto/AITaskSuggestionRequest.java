package com.taskscheduler.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AITaskSuggestionRequest {
    @NotBlank(message = "User message cannot be blank")
    private String userMessage;

    @NotNull(message = "User ID cannot be null")
    private Long userId;
}
