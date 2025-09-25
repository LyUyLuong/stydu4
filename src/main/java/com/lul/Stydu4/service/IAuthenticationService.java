package com.lul.Stydu4.service;

import com.lul.Stydu4.dto.request.AuthenticationRequest;

public interface IAuthenticationService {
    boolean authenticate(AuthenticationRequest authenticationRequest);
}
