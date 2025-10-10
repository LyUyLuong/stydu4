package com.lul.Stydu4.dto.response.Answer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerSummaryResponse {

    private String id;

    private Boolean isCorrect;

    private String mark;

    private String questionId;

}
