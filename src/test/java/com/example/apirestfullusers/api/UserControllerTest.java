package com.example.apirestfullusers.api;

import com.example.apirestfullusers.api.dto.UserRegistrationRequest;
import com.example.apirestfullusers.api.dto.UserResponse;
import com.example.apirestfullusers.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void shouldCreateUser() throws Exception {
        var request = new UserRegistrationRequest();
        request.setName("David");
        request.setEmail("david.alcaino.7@gmail.com");
        request.setPassword("Da123456");
        request.setPhones(List.of());

        var response = new UserResponse();
        response.setId(UUID.randomUUID());
        response.setName(request.getName());
        response.setEmail(request.getEmail());
        response.setCreated(LocalDateTime.now());
        response.setModified(response.getCreated());
        response.setLastLogin(response.getCreated());
        response.setToken(UUID.randomUUID().toString());
        response.setInactive(false);

        when(userService.register(any(UserRegistrationRequest.class))).thenReturn(response);

        mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }
}
