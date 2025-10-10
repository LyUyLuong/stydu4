package com.lul.Stydu4.dto.request.PartTest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartTestUpdateRequest {

    private String id;
    private String name;
    private String description;
    private String type;

    private List<String> questionIds;
    private List<String> questionGroupsIds;
}
