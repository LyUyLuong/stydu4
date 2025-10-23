package com.lul.Stydu4.dto.response.Question;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionTestSummaryResponse {

    private String id;
    private String name;
    private String content;
    private String type;
    private String description;

    // ❌ OLD: Remove
    // private String audioPath;
    // private String image;

    // ✅ NEW: File references
    private String imageId;
    private String imageUrl;
    private String audioId;
    private String audioUrl;

    private String partTestId;
    private String questionGroupId;
    private Integer answersCount;
}
