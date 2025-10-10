package com.lul.Stydu4.dto.response.Question;

import com.lul.Stydu4.dto.response.Answer.AnswerDetailResponse;
import com.lul.Stydu4.entity.AnswerEntity;
import com.lul.Stydu4.entity.PartTestEntity;
import com.lul.Stydu4.entity.QuestionGroupEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
public class QuestionDetailResponse {

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
