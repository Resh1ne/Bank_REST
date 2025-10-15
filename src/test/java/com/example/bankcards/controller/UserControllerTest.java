package com.example.bankcards.controller;

import com.example.bankcards.config.SecurityConfig;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.security.CustomUserDetails;
import com.example.bankcards.security.CustomUserDetailsService;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.security.WithMockCustomUser;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@DisplayName("User Controller Tests")
class UserControllerTest {

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

    @Nested
    @DisplayName("Get User Cards Endpoint Tests")
    class GetUserCardsTests {
        @Test
        @DisplayName("Should return 200 OK when USER requests their own cards")
        @WithMockCustomUser(id = 1L, username = "testuser", roles = "USER")
        void getUserCards_AsOwner_Success() throws Exception {
            Page<CardDto> cardPage = new PageImpl<>(Collections.singletonList(new CardDto()));
            when(cardService.getCardsByUserId(eq(1L), any(), any(), any(Pageable.class))).thenReturn(cardPage);

            mockMvc.perform(get("/api/users/1/cards"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return 403 Forbidden when USER requests another user's cards")
        @WithMockCustomUser(id = 1L, username = "testuser", roles = "USER")
        void getUserCards_AsDifferentUser_Forbidden() throws Exception {
            mockMvc.perform(get("/api/users/99/cards"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 200 OK when ADMIN requests any user's cards")
        @WithMockUser(roles = "ADMIN")
        void getUserCards_AsAdmin_Success() throws Exception {
            Page<CardDto> cardPage = new PageImpl<>(Collections.singletonList(new CardDto()));
            when(cardService.getCardsByUserId(anyLong(), any(), any(), any(Pageable.class))).thenReturn(cardPage);

            mockMvc.perform(get("/api/users/99/cards"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return 403 Forbidden when anonymous user requests cards")
        void getUserCards_AsAnonymous_Forbidden() throws Exception {
            mockMvc.perform(get("/api/users/1/cards"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Request Card Block Endpoint Tests")
    class RequestCardBlockTests {

        @Test
        @DisplayName("Should return 200 OK when USER requests to block their card")
        @WithMockUser(username = "testuser", roles = "USER")
        void requestBlock_AsUser_Success() throws Exception {
            CardDto blockedCardDto = new CardDto();
            blockedCardDto.setId(15L);
            blockedCardDto.setStatus(CardStatus.BLOCKED.name());

            when(cardService.requestCardBlock(15L, "testuser")).thenReturn(blockedCardDto);

            mockMvc.perform(post("/api/users/cards/15/block-request")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("BLOCKED")));
        }

        @Test
        @DisplayName("Should return 403 Forbidden when ADMIN requests to block a card")
        @WithMockUser(roles = "ADMIN")
        void requestBlock_AsAdmin_Forbidden() throws Exception {
            mockMvc.perform(post("/api/users/cards/15/block-request")
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }
}