package com.serviceco.serviceco_auth_service.services;

import com.serviceco.serviceco_auth_service.exception.EmailAlreadyExistsException;
import com.serviceco.serviceco_auth_service.model.User;
import com.serviceco.serviceco_auth_service.model.UserRole;
import com.serviceco.serviceco_auth_service.model.dto.RegistrationRequest;
import com.serviceco.serviceco_auth_service.model.dto.RegistrationResponse;
import com.serviceco.serviceco_auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public RegistrationResponse register(RegistrationRequest request) {

        // 1. Normalize email
        String email = request.email()
                .trim()
                .toLowerCase();

        // 2. Check whether email already exists
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(
                    "Email is already registered"
            );
        }

        // 3. Prevent self-registration as ADMIN
        if (request.role() == UserRole.ADMIN) {
            throw new IllegalArgumentException(
                    "ADMIN registration is not allowed"
            );
        }

        // 4. Hash password
        String passwordHash =
                passwordEncoder.encode(request.password());

        // 5. Create User entity
        User user = User.builder()
                .name(request.name().trim())
                .email(email)
                .passwordHash(passwordHash)
                .role(request.role())
                .createdAt(LocalDateTime.now())
                .build();

        // 6. Save user
        User savedUser = userRepository.save(user);

        // 7. Return safe response
        return new RegistrationResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole()
        );
    }
}