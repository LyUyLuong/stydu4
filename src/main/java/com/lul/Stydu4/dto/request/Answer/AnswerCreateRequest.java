package com.lul.Stydu4.dto.request.Answer;

import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "Answer content is required")
    private String content;

    @NotNull(message = "isCorrect flag is required")
    private Boolean isCorrect;

    private String mark;
}
