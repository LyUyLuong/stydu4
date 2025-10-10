package com.lul.Stydu4.dto.response.PartTest;

import com.lul.Stydu4.dto.response.Question.QuestionDetailResponse;
import com.lul.Stydu4.dto.response.QuestionGroupResponse.QuestionGroupDetailResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartTestDetailResponse {

    private String id;
    private String name;
    private String description;
    private String type;
    private String testId; // null nếu chưa gán test nào

    private List<QuestionDetailResponse> questions;
    private List<QuestionGroupDetailResponse> questionGroups;
}
