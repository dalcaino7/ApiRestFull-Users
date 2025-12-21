package com.example.apirestfullusers.service;

import com.example.apirestfullusers.api.dto.PhoneRequest;
import com.example.apirestfullusers.api.dto.UserRegistrationRequest;
import com.example.apirestfullusers.domain.UserEntity;
import com.example.apirestfullusers.exception.DuplicateEmailException;
import com.example.apirestfullusers.exception.InvalidFieldException;
import com.example.apirestfullusers.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        // Regex simples para las pruebas
        userService = new UserService(userRepository, "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", "^[A-Za-z].{5,}$");
    }

    @Test
    void registerShouldPersistUserAndReturnResponse() {
        var request = buildRequest("david.alcaino.7@gmail.com", "Da123456");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenAnswer(inv -> {
            var entity = (UserEntity) inv.getArgument(0);
            // Simula asignación de id
            try {
                var field = UserEntity.class.getDeclaredField("id");
                field.setAccessible(true);
                field.set(entity, UUID.randomUUID());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return entity;
        });

        var response = userService.register(request);

        assertEquals(request.getEmail(), response.getEmail());
        assertEquals(request.getName(), response.getName());
        assertNotNull(response.getId());
        assertNotNull(response.getCreated());
        assertNotNull(response.getToken());
        assertFalse(response.isInactive());
        assertEquals(1, response.getPhones().size());

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        assertEquals(request.getPhones().get(0).getNumber(), captor.getValue().getPhones().get(0).getNumber());
    }

    @Test
    void registerShouldFailWhenEmailExists() {
        var request = buildRequest("existing@correo.com", "1234567");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new UserEntity()));

        assertThrows(DuplicateEmailException.class, () -> userService.register(request));
    }

    @Test
    void registerShouldFailOnInvalidEmail() {
        var request = buildRequest("bad-email", "1234567");

        assertThrows(InvalidFieldException.class, () -> userService.register(request));
        verifyNoInteractions(userRepository);
    }

    @Test
    void registerShouldFailOnInvalidPassword() {
        var request = buildRequest("ok@mail.com", "short");

        assertThrows(InvalidFieldException.class, () -> userService.register(request));
        verifyNoInteractions(userRepository);
    }

    private UserRegistrationRequest buildRequest(String email, String password) {
        var req = new UserRegistrationRequest();
        req.setName("David");
        req.setEmail(email);
        req.setPassword(password);
        var phone = new PhoneRequest();
        phone.setNumber("1234567");
        phone.setCityCode("1");
        phone.setCountryCode("57");
        req.setPhones(List.of(phone));
        return req;
    }
}
