package com.lul.Stydu4.dto.request.Course;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseCreationRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", message = "Price must be greater than or equal to 0")
    private BigDecimal price;

    private String imageUrl;

    @Min(value = 1, message = "Duration must be at least 1 day")
    private Integer duration; // Số ngày

    private List<String> testIds; // List IDs của các Test
}
