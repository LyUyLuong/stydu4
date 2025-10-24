package com.lul.Stydu4.dto.request.Exam;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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

    @Valid
    @NotEmpty(message = "Answer list cannot be empty")
    List<UserAnswerSubmit> answers;
}
