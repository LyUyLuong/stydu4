package com.lul.Stydu4.dto.response.QuestionGroupResponse;

import com.lul.Stydu4.dto.response.Question.QuestionDetailResponse;
import com.lul.Stydu4.entity.PartTestEntity;
import com.lul.Stydu4.entity.QuestionTestEntity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
    private String audioPath;
    private String image;

    private String partTestId;

    private List<QuestionDetailResponse> questions;

}
