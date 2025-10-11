package com.lul.Stydu4.dto.request.Test;

import com.lul.Stydu4.enums.TestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCreationRequest {

    private String name;
    private Integer status;
    private String type;
    private String description;
    private Long numberOfParticipants=0L;

}
