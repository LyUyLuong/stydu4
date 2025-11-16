package com.lul.Stydu4.dto.request.Exam;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubmitExamRequest {

    @NotBlank(message = "Test ID is required")
    String testId;

    // Danh sách part IDs được làm (có thể là subset của test)
    List<String> partIds;

    // Allow empty list (user didn't answer any questions)
    @Valid
    @NotNull(message = "Answer list is required")
    List<UserAnswerSubmit> answers;
}
