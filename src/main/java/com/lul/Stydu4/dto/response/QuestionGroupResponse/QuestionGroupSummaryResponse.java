package com.lul.Stydu4.dto.response.QuestionGroupResponse;

import com.lul.Stydu4.dto.response.Question.QuestionDetailResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionGroupSummaryResponse {

    private String id;

    private String name;
    private String content;
    private String type;
    private String audioPath;
    private String image;

    private String partTestId;

    private Integer questionsCount;

}
