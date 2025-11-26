package com.lul.Stydu4.dto.response.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentTestResponse {
    private String id;
    private String name;
    private String type;
    private Integer status;
    private Integer numberOfParticipants;
}
