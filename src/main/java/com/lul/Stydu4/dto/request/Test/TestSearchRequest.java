package com.lul.Stydu4.dto.request.Test;


import com.lul.Stydu4.enums.TestType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestSearchRequest {

    private String name;
    private String type;
    private Integer status;

    @Min(value = 1, message = "Page must >= 1")
    private Integer page = 1;

    @Min(value = 1, message = "Size must >= 1")
    @Max(value = 100, message = "Size must <= 100")
    private Integer size = 5;

}
