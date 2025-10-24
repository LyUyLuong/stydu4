package com.lul.Stydu4.dto.response.QuestionGroupResponse;

import com.lul.Stydu4.dto.response.Question.QuestionTestDetailResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionGroupDetailResponse {

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
    private List<QuestionTestDetailResponse> questions;
}
