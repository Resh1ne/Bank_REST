package com.example.bankcards.controller;

import com.example.bankcards.config.SecurityConfig;
import com.example.bankcards.dto.TransactionDto;
import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.security.CustomUserDetailsService;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@Import(SecurityConfig.class)
@DisplayName("Transaction Controller Tests")
class TransactionControllerTest {
    @TestConfiguration
    static class TestConfig {
        @Bean
        public UserDetailsService userDetailsService() {
            return Mockito.mock(CustomUserDetailsService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @DisplayName("Should create transfer and return 201 Created for USER role")
    @WithMockUser(username = "testuser", roles = "USER")
    void createTransfer_AsUser_Success() throws Exception {
        TransferRequestDto requestDto = new TransferRequestDto();
        requestDto.setFromCardId(1L);
        requestDto.setToCardId(2L);
        requestDto.setAmount(new BigDecimal("100.00"));
        TransactionDto responseDto = new TransactionDto();
        responseDto.setId(99L);
        responseDto.setStatus("COMPLETED");
        responseDto.setAmount(new BigDecimal("100.00"));
        responseDto.setCreatedAt(LocalDateTime.now());
        when(cardService.transferBetweenOwnCards(anyString(), any(TransferRequestDto.class))).thenReturn(responseDto);

        ResultActions response = mockMvc.perform(post("/api/transactions/transfer")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));

        response.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(99)))
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andExpect(jsonPath("$.amount", is(100.00)))
                .andExpect(jsonPath("$.createdAt", notNullValue()));
    }

    @Test
    @DisplayName("Should return 400 Bad Request for invalid transfer request")
    @WithMockUser(roles = "USER")
    void createTransfer_InvalidRequest_BadRequest() throws Exception {
        TransferRequestDto requestDto = new TransferRequestDto();
        requestDto.setFromCardId(1L);
        requestDto.setToCardId(2L);
        requestDto.setAmount(new BigDecimal("-100.00"));

        mockMvc.perform(post("/api/transactions/transfer")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount", is("Amount must be positive")));
    }

    @Test
    @DisplayName("Should return 403 Forbidden when creating transfer as ADMIN")
    @WithMockUser(roles = "ADMIN")
    void createTransfer_AsAdmin_Forbidden() throws Exception {
        TransferRequestDto requestDto = new TransferRequestDto();
        requestDto.setFromCardId(1L);
        requestDto.setToCardId(2L);
        requestDto.setAmount(new BigDecimal("100.00"));

        mockMvc.perform(post("/api/transactions/transfer")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 403 Forbidden when creating transfer as anonymous")
    void createTransfer_AsAnonymous_Unauthorized() throws Exception {
        TransferRequestDto requestDto = new TransferRequestDto();
        requestDto.setFromCardId(1L);
        requestDto.setToCardId(2L);
        requestDto.setAmount(new BigDecimal("100.00"));

        mockMvc.perform(post("/api/transactions/transfer")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }
}