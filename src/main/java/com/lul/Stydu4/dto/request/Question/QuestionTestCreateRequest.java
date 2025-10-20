package com.lul.Stydu4.dto.request.Question;

import com.lul.Stydu4.dto.request.Answer.AnswerCreateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class QuestionTestCreateRequest {

    @NotBlank(message = "Question name is required")
    private String name;

    private String content;

    @NotBlank(message = "Question type is required")
    private String type;

    private String audioPath;
    private String image;
    private String description;

    private String partId;
    private String questionGroupId;

    // Thay đổi từ answerIds thành answers object
    @Valid
    @NotNull(message = "Answers list is required")
    @Size(min = 1, max = 10, message = "Must have between 1 and 10 answers")
    @Builder.Default
    private List<AnswerCreateRequest> answers = new ArrayList<>();
}
