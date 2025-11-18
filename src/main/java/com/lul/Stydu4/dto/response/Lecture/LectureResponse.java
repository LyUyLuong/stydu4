package com.lul.Stydu4.dto.response.Lecture;

import com.lul.Stydu4.enums.LectureType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureResponse {

    private String id;
    private String courseId;
    private String courseTitle;
    private String title;
    private String description;
    private Integer orderIndex;
    private LectureType type;
    private String content;
    private Integer duration;
    private Boolean isPublished;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
