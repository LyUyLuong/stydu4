package com.lul.Stydu4.dto.response.Question;

import com.lul.Stydu4.dto.response.Answer.AnswerDetailResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionTestDetailResponse {

    private String id;

    private String name;
    private String content;
    private String type;
    private String audioPath;
    private String image;
    private String description;

    private String partTestId;

    private String questionGroupId;

    @Builder.Default
    private List<AnswerDetailResponse> answers = new ArrayList<>();

}
