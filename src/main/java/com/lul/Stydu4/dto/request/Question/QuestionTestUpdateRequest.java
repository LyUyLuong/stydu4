package com.lul.Stydu4.dto.request.Question;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionTestUpdateRequest {

    private String id;
    private String name;
    private String content;
    private String type;

    private String imageId;
    private String audioId;

    private String description;

    private String questionGroupId;
    private String partId;
    private List<String> answerIds;


}
