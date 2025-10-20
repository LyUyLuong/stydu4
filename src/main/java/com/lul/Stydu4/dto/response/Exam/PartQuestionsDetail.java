package com.lul.Stydu4.dto.response.Exam;

import com.lul.Stydu4.dto.response.Question.QuestionTestDetailResponse;
import com.lul.Stydu4.dto.response.QuestionGroupResponse.QuestionGroupDetailResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PartQuestionsDetail {

    String partId;
    String partName;
    String partType;
    String description;

    List<QuestionTestDetailResponse> questions;
    List<QuestionGroupDetailResponse> questionGroups;
}
