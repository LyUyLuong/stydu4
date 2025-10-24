package com.lul.Stydu4.dto.request.Exam;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserAnswerSubmit {

    @NotBlank(message = "Question ID is required")
    String questionId;

    @NotBlank(message = "Answer ID is required")
    String answerId;
}
