package com.lul.Stydu4.dto.request.PartTest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartTestCreationRequest {

    private String name;
    private String description;
    private String type;
    private String testId;

}
