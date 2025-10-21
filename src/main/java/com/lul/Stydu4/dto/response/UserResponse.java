package com.lul.Stydu4.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private LocalDate dob;
    private String email;
    private String phoneNumber;

    private String authProvider;
    private String providerId;
    private Set<RoleResponse> roles;

}
