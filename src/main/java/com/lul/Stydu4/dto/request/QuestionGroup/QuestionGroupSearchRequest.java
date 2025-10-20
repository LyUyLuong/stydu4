package com.lul.Stydu4.dto.request.QuestionGroup;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionGroupSearchRequest {

    private String name;

    private String type;

    private String partName;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) // Format: yyyy-MM-dd
    private LocalDate createdFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate createdTo;

    @Min(value = 1, message = "Page must >= 1")
    private Integer page;

    @Min(value = 1, message = "Size must be >= 1")
    @Max(value = 100, message = "Size must <= 100")
    private Integer size=5;

}
