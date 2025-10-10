package com.lul.Stydu4.dto.response.Test;

import com.lul.Stydu4.dto.response.PartTest.PartTestDetailResponse;
import com.lul.Stydu4.entity.QuestionGroupEntity;
import com.lul.Stydu4.entity.QuestionTestEntity;
import com.lul.Stydu4.enums.TestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestSummaryResponse {

    private String id;
    private String name;
    private String description;
    private Integer status;
    private Long numberOfParticipants;
    private String audioPath;
    private TestType type;
    private String slug;

    private Integer partsCount;

}
