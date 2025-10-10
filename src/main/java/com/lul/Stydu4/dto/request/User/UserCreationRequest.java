package com.lul.Stydu4.dto.request.User;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCreationRequest {

    @Size(min = 5,message = "USERNAME_INVALID")
    private String username;
    private String firstName;
    private String lastName;

    @Size(min = 6, message = "INVALID_PASSWORD")
    private String password;
    private LocalDate dob;

}
