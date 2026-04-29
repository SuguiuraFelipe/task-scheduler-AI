package com.taskscheduler.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskFromPromptRequest {
    @NotBlank(message = "Prompt não pode estar vazio")
    private String prompt;
}
