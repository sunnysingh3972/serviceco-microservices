package com.serviceco.serviceco_auth_service.services;

import com.serviceco.serviceco_auth_service.model.dto.LoginRequest;
import com.serviceco.serviceco_auth_service.model.dto.LoginResponse;

public interface AuthenticationService {
    LoginResponse login(LoginRequest request);
}
