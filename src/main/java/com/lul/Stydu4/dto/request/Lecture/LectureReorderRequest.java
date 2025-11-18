package com.lul.Stydu4.dto.request.Lecture;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureReorderRequest {

    @NotEmpty(message = "Lecture IDs are required")
    private List<String> lectureIds; // List of lecture IDs in new order
}
