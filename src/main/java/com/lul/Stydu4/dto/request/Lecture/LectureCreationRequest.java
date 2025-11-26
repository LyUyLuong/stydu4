package com.lul.Stydu4.dto.request.Lecture;

import com.lul.Stydu4.enums.LectureType;
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
public class LectureCreationRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Lecture type is required")
    private LectureType type;

    private String content; // URL video, text content, or file URL

    private Integer duration; // Duration in minutes (for VIDEO type)
}
