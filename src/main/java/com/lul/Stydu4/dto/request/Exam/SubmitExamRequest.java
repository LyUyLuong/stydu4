package com.lul.Stydu4.dto.request.Exam;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
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

    // Thời gian bắt đầu làm bài (được gửi từ frontend)
    // Frontend lưu timestamp khi user click "Start Test" và gửi lên khi submit
    // Sử dụng Instant thay vì LocalDateTime để support ISO 8601 format từ JavaScript (có timezone Z)
    @NotNull(message = "Start time is required")
    Instant startedAt;

    // ✅ NEW: Thời gian thực tế làm bài (tính từ client, đơn vị: giây)
    // Frontend tính duration từ startedAt đến thời điểm submit để đảm bảo chính xác
    // Nếu có giá trị này, backend sẽ ưu tiên sử dụng thay vì tính từ startedAt và now
    Long durationSeconds;
}
