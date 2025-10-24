package com.lul.Stydu4.dto.request.Answer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerUpdateRequest {

    private Long id;
    private String content;
    private Boolean isCorrect;
    private String mark;
    private String questionId;
}
