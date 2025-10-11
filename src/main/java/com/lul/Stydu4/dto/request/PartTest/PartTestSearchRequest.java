package com.lul.Stydu4.dto.request.PartTest;

import com.lul.Stydu4.enums.PartType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartTestSearchRequest {

    private String name;
    private String description;

    private String type;
    private String testName;

    @Min(value = 1, message = "Page must >= 1")
    private Integer page;

    @Min(value = 1, message = "Size must be >= 1")
    @Max(value = 100, message = "Size must <= 100")
    private Integer size=5;
}
