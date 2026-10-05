package com.serviceco.serviceco_auth_service.services;

import com.serviceco.serviceco_auth_service.model.User;

public interface JwtService {

    String generateToken(CustomUserDetails  user);
}