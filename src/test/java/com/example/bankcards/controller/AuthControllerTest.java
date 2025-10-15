package com.example.bankcards.controller;

import com.example.bankcards.dto.LoginRequestDto;
import com.example.bankcards.dto.RegistrationRequestDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.exception.UserAlreadyExistsException;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Auth Controller Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @Nested
    @DisplayName("User Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("Should return 201 Created when user is registered successfully")
        void registerUser_Success() throws Exception {
            RegistrationRequestDto requestDto = new RegistrationRequestDto();
            requestDto.setUsername("testuser");
            requestDto.setEmail("test@example.com");
            requestDto.setPassword("password123");
            UserDto responseDto = new UserDto();
            responseDto.setUsername("testuser");
            responseDto.setEmail("test@example.com");
            when(userService.registerNewUser(any(RegistrationRequestDto.class)))
                    .thenReturn(responseDto);

            ResultActions response = mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)));

            response.andExpect(status().isCreated())
                    .andExpect(jsonPath("$.username", is(responseDto.getUsername())))
                    .andExpect(jsonPath("$.email", is(responseDto.getEmail())));
        }

        @Test
        @DisplayName("Should return 409 Conflict when username is already taken")
        void registerUser_UsernameConflict() throws Exception {
            RegistrationRequestDto requestDto = new RegistrationRequestDto();
            requestDto.setUsername("existingUser");
            requestDto.setEmail("test@example.com");
            requestDto.setPassword("password123");
            when(userService.registerNewUser(any(RegistrationRequestDto.class)))
                    .thenThrow(new UserAlreadyExistsException("Username existingUser is already taken."));

            ResultActions response = mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)));

            response.andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error", is("Username existingUser is already taken.")));
        }
    }

    @Nested
    @DisplayName("User Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should return 200 OK and JWT when credentials are valid")
        void authenticateUser_Success() throws Exception {
            LoginRequestDto loginRequest = new LoginRequestDto();
            loginRequest.setUsername("testuser");
            loginRequest.setPassword("password123");
            UserDetails userDetails = new User("testuser", "password", Collections.emptyList());
            Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            String expectedToken = "mock.jwt.token";
            when(authenticationManager.authenticate(any())).thenReturn(auth);
            when(jwtService.generateToken(userDetails)).thenReturn(expectedToken);

            ResultActions response = mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)));

            response.andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken", is(expectedToken)));
        }
    }
}
