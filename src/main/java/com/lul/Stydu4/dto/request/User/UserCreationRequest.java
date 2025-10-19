package com.lul.Stydu4.dto.request.User;

import com.lul.Stydu4.validator.DobConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCreationRequest {

    @NotBlank(message = "USERNAME_REQUIRED")
    @Size(min = 5,message = "INVALID_USERNAME")
    private String username;

    private String firstName;
    private String lastName;

    @NotBlank(message = "PASSWORD_REQUIRED")
    @Size(min = 6, message = "INVALID_PASSWORD")
    private String password;

    @DobConstraint(min = 16, message = "INVALID_DOB")
    private LocalDate dob;

}
