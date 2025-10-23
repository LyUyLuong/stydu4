package com.lul.Stydu4.dto.response.QuestionGroupResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionGroupSummaryResponse {

    private String id;
    private String name;
    private String content;
    private String type;

    // ❌ OLD: Remove
    // private String audioPath;
    // private String image;

    // ✅ NEW: File references
    private String imageId;
    private String imageUrl;
    private String audioId;
    private String audioUrl;

    private String partTestId;
    private Integer questionsCount;
}
