package com.lul.Stydu4.dto.request.Answer;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerCreateRequest {

    // Content can be empty for some question types (e.g., Part 1, Part 2 only have marks)
    private String content;

    @NotNull(message = "isCorrect flag is required")
    private Boolean isCorrect;

    private String mark;
}
