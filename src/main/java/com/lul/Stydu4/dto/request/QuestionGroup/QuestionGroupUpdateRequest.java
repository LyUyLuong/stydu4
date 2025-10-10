package com.lul.Stydu4.dto.request.QuestionGroup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionGroupUpdateRequest {

    private String id;
    private String name;
    private String content;
    private String type;
    private String audioPath;
    private String image;

    private List<String> questionIds;
}
