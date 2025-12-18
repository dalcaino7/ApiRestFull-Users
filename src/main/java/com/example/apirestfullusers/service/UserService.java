package com.example.apirestfullusers.service;

import com.example.apirestfullusers.api.dto.PhoneRequest;
import com.example.apirestfullusers.api.dto.PhoneResponse;
import com.example.apirestfullusers.api.dto.UserRegistrationRequest;
import com.example.apirestfullusers.api.dto.UserResponse;
import com.example.apirestfullusers.domain.PhoneEntity;
import com.example.apirestfullusers.domain.UserEntity;
import com.example.apirestfullusers.exception.DuplicateEmailException;
import com.example.apirestfullusers.exception.InvalidFieldException;
import com.example.apirestfullusers.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final Pattern emailPattern;
    private final Pattern passwordPattern;

    public UserService(
            UserRepository userRepository,
            @Value("${user.email.regex}") String emailRegex,
            @Value("${user.password.regex}") String passwordRegex
    ) {
        this.userRepository = userRepository;
        this.emailPattern = Pattern.compile(emailRegex);
        this.passwordPattern = Pattern.compile(passwordRegex);
    }

    @Transactional
    public UserResponse register(UserRegistrationRequest request) {
        validateEmail(request.getEmail());
        validatePassword(request.getPassword());
        userRepository.findByEmail(request.getEmail())
                .ifPresent(user -> {
                    throw new DuplicateEmailException("El correo ya esta registrado");
                });

        var now = LocalDateTime.now();
        var user = new UserEntity();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setToken(UUID.randomUUID().toString());
        user.setInactive(false);
        user.setCreated(now);
        user.setModified(now);
        user.setLastLogin(now);

        for (PhoneRequest phoneRequest : request.getPhones()) {
            var phone = new PhoneEntity();
            phone.setNumber(phoneRequest.getNumber());
            phone.setCityCode(phoneRequest.getCityCode());
            phone.setCountryCode(phoneRequest.getCountryCode());
            phone.setUser(user);
            user.getPhones().add(phone);
        }

        var saved = userRepository.save(user);
        return toResponse(saved);
    }

    private void validateEmail(String email) {
        if (!emailPattern.matcher(email).matches()) {
            throw new InvalidFieldException("El correo no tiene un formato válido");
        }
    }

    private void validatePassword(String password) {
        if (!passwordPattern.matcher(password).matches()) {
            throw new InvalidFieldException("La contraseña no cumple el formato requerido");
        }
    }

    private UserResponse toResponse(UserEntity entity) {
        var response = new UserResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setEmail(entity.getEmail());
        response.setCreated(entity.getCreated());
        response.setModified(entity.getModified());
        response.setLastLogin(entity.getLastLogin());
        response.setToken(entity.getToken());
        response.setInactive(entity.isInactive());
        for (PhoneEntity phoneEntity : entity.getPhones()) {
            response.getPhones().add(
                    new PhoneResponse(
                            phoneEntity.getNumber(),
                            phoneEntity.getCityCode(),
                            phoneEntity.getCountryCode()
                    )
            );
        }
        return response;
    }
}
