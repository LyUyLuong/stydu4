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
public class StartExamRequest {

    @NotBlank(message = "Test ID is required")
    String testId;

    // Danh sách part IDs - nếu empty hoặc null thì là full test
    List<String> partIds;
}
