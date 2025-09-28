package com.lul.Stydu4.dto.request;

import lombok.*;

import java.time.LocalDate;
import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequest {
    private String firstName;
    private String lastName;
    private String password;
    private LocalDate dob;
    private List<String> roles;

}
