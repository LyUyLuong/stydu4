package com.lul.Stydu4.dto.request.Test;

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
public class TestUpdateRequest {

    private String name;
    private String description;
    private Integer status;
    private Long numberOfParticipants;
    private String audioPath;
    private String type;
    private String slug;

    private List<String> partTestIds;

}
