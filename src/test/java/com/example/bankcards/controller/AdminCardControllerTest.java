package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.security.CustomUserDetailsService;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminCardController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@DisplayName("Admin Card Controller Tests (Security Disabled)")
class AdminCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CardService cardService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("Should create card and return 201 Created")
    void createCard_Success() throws Exception {
        CreateCardRequest requestDto = new CreateCardRequest();
        requestDto.setOwnerId(1L);
        requestDto.setHolderName("TEST HOLDER");
        requestDto.setExpiryDate("12/2030");
        CardDto responseDto = new CardDto();
        responseDto.setId(1L);
        when(cardService.createCard(any(CreateCardRequest.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @DisplayName("Should get all cards and return 200 OK")
    void getAllCards_Success() throws Exception {
        Page<CardDto> cardPage = new PageImpl<>(Collections.singletonList(new CardDto()));
        when(cardService.getAllCards(any(Pageable.class))).thenReturn(cardPage);

        mockMvc.perform(get("/api/admin/cards")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(1)));
    }

    @Test
    @DisplayName("Should block card and return 200 OK")
    void blockCard_Success() throws Exception {
        when(cardService.blockCard(1L)).thenReturn(new CardDto());
        mockMvc.perform(post("/api/admin/cards/1/block"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should activate card and return 200 OK")
    void activateCard_Success() throws Exception {
        when(cardService.activateCard(1L)).thenReturn(new CardDto());
        mockMvc.perform(post("/api/admin/cards/1/activate"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should delete card and return 204 No Content")
    void deleteCard_Success() throws Exception {
        mockMvc.perform(delete("/api/admin/cards/1"))
                .andExpect(status().isNoContent());
    }
}