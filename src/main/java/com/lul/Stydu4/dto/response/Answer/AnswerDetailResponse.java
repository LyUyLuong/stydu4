package com.lul.Stydu4.dto.response.Answer;

import com.lul.Stydu4.entity.QuestionTestEntity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerDetailResponse {

    private String id;

    private String content;

    private Boolean isCorrect;

    private String mark;

    private String questionId;

}
