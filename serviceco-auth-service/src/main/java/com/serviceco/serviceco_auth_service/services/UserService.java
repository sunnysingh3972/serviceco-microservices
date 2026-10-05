package com.serviceco.serviceco_auth_service.services;

import com.serviceco.serviceco_auth_service.model.dto.RegistrationRequest;
import com.serviceco.serviceco_auth_service.model.dto.RegistrationResponse;

public interface UserService {

    RegistrationResponse register(RegistrationRequest request);
}