package com.lul.Stydu4.dto.request.Lecture;

import com.lul.Stydu4.enums.LectureType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureUpdateRequest {

    private String title;

    private String description;

    private LectureType type;

    private String content;

    private Integer duration;

    private Integer orderIndex;

    private Boolean isPublished;
}
