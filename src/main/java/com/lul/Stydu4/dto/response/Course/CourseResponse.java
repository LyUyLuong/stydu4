package com.lul.Stydu4.dto.response.Course;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponse {
    
    private String id;
    private String title;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private Integer duration;
    private Boolean isPublished;
    private Integer testCount;
    private Integer studentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
