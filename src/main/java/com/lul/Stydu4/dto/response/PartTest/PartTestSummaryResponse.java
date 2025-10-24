package com.lul.Stydu4.dto.response.PartTest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartTestSummaryResponse {

    private String id;
    private String name;
    private String description;
    private String type;
    private String testId;

    private Integer questionsCount;
    private Integer questionGroupsCount;

}
