package com.example.bankcards.service.impl;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.dto.TransactionDto;
import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.InvalidOperationException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.mapper.CardMapper;
import com.example.bankcards.util.mapper.TransactionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Card Service Implementation Tests")
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private CardMapper cardMapper;
    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private CardServiceImpl cardService;

    private User testUser;
    private Card cardFrom;
    private Card cardTo;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        cardFrom = new Card();
        cardFrom.setId(10L);
        cardFrom.setOwner(testUser);
        cardFrom.setStatus(CardStatus.ACTIVE);
        cardFrom.setBalance(new BigDecimal("1000.00"));
        cardFrom.setPan("1111222233334444");
        cardFrom.setPanLast4("4444");

        cardTo = new Card();
        cardTo.setId(20L);
        cardTo.setOwner(testUser);
        cardTo.setStatus(CardStatus.ACTIVE);
        cardTo.setBalance(new BigDecimal("500.00"));
    }

    @Nested
    @DisplayName("Transfer Tests")
    class TransferTests {
        @Test
        @DisplayName("Should transfer money successfully")
        void transferBetweenOwnCards_Success() {
            TransferRequestDto request = new TransferRequestDto();
            request.setFromCardId(10L);
            request.setToCardId(20L);
            request.setAmount(new BigDecimal("100.00"));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(cardRepository.findById(10L)).thenReturn(Optional.of(cardFrom));
            when(cardRepository.findById(20L)).thenReturn(Optional.of(cardTo));
            when(transactionMapper.toDto(any())).thenReturn(new TransactionDto());

            cardService.transferBetweenOwnCards("testuser", request);

            assertEquals(new BigDecimal("900.00"), cardFrom.getBalance());
            assertEquals(new BigDecimal("600.00"), cardTo.getBalance());
            verify(cardRepository, times(2)).save(any(Card.class));
            verify(transactionRepository, times(1)).save(any(Transaction.class));
        }

        @Test
        @DisplayName("Should throw InsufficientFundsException for transfer")
        void transferBetweenOwnCards_InsufficientFunds() {
            TransferRequestDto request = new TransferRequestDto();
            request.setFromCardId(10L);
            request.setToCardId(20L);
            request.setAmount(new BigDecimal("2000.00"));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(cardRepository.findById(10L)).thenReturn(Optional.of(cardFrom));
            when(cardRepository.findById(20L)).thenReturn(Optional.of(cardTo));

            assertThrows(InsufficientFundsException.class, () -> cardService.transferBetweenOwnCards("testuser", request));
            verify(cardRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw InvalidOperationException when 'from' card is blocked")
        void transferBetweenOwnCards_CardBlocked() {
            cardFrom.setStatus(CardStatus.BLOCKED);
            TransferRequestDto request = new TransferRequestDto();
            request.setFromCardId(10L);
            request.setToCardId(20L);
            request.setAmount(new BigDecimal("100.00"));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(cardRepository.findById(10L)).thenReturn(Optional.of(cardFrom));
            when(cardRepository.findById(20L)).thenReturn(Optional.of(cardTo));

            assertThrows(InvalidOperationException.class, () -> cardService.transferBetweenOwnCards("testuser", request));
        }
    }

    @Nested
    @DisplayName("Admin Card Management Tests")
    class AdminActionsTests {
        @Test
        @DisplayName("Should create card successfully")
        void createCard_Success() {
            CreateCardRequest request = new CreateCardRequest();
            request.setOwnerId(1L);
            request.setHolderName("TEST USER");
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(cardMapper.toEntity(request)).thenReturn(new Card());
            when(cardRepository.save(any(Card.class))).thenReturn(new Card());
            when(cardMapper.toDto(any(Card.class))).thenReturn(new CardDto());

            CardDto result = cardService.createCard(request);

            assertNotNull(result);
            verify(cardRepository, times(1)).save(any(Card.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when creating card for non-existent user")
        void createCard_UserNotFound() {
            CreateCardRequest request = new CreateCardRequest();
            request.setOwnerId(99L);
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> cardService.createCard(request));
            verify(cardRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should block card successfully")
        void blockCard_Success() {
            when(cardRepository.findById(10L)).thenReturn(Optional.of(cardFrom));
            when(cardRepository.save(any(Card.class))).thenReturn(cardFrom);

            cardService.blockCard(10L);

            assertEquals(CardStatus.BLOCKED, cardFrom.getStatus());
            verify(cardRepository, times(1)).save(cardFrom);
        }

        @Test
        @DisplayName("Should activate card successfully")
        void activateCard_Success() {
            cardFrom.setStatus(CardStatus.BLOCKED);
            when(cardRepository.findById(10L)).thenReturn(Optional.of(cardFrom));
            when(cardRepository.save(any(Card.class))).thenReturn(cardFrom);

            cardService.activateCard(10L);

            assertEquals(CardStatus.ACTIVE, cardFrom.getStatus());
            verify(cardRepository, times(1)).save(cardFrom);
        }

        @Test
        @DisplayName("Should delete card successfully")
        void deleteCard_Success() {
            when(cardRepository.existsById(10L)).thenReturn(true);

            cardService.deleteCard(10L);

            verify(cardRepository, times(1)).deleteById(10L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when deleting non-existent card")
        void deleteCard_NotFound() {
            when(cardRepository.existsById(99L)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class, () -> cardService.deleteCard(99L));
            verify(cardRepository, never()).deleteById(anyLong());
        }
    }

    @Nested
    @DisplayName("User Card Actions Tests")
    class UserActionsTests {
        @Test
        @DisplayName("Should get cards by user ID with filters")
        void getCardsByUserId_Success() {
            Pageable pageable = Pageable.unpaged();
            Page<Card> cardPage = new PageImpl<>(Collections.singletonList(cardFrom));
            when(userRepository.existsById(1L)).thenReturn(true);
            when(cardRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(cardPage);
            when(cardMapper.toDto(any(Card.class))).thenReturn(new CardDto());

            Page<CardDto> result = cardService.getCardsByUserId(1L, CardStatus.ACTIVE, "4444", pageable);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            verify(cardRepository, times(1)).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when getting cards for non-existent user")
        void getCardsByUserId_UserNotFound() {
            when(userRepository.existsById(99L)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class, () -> cardService.getCardsByUserId(99L, null, null, Pageable.unpaged()));
        }

        @Test
        @DisplayName("Should allow user to request block for their own card")
        void requestCardBlock_Success() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(cardRepository.findById(10L)).thenReturn(Optional.of(cardFrom));
            when(cardRepository.save(any(Card.class))).thenReturn(cardFrom);

            cardService.requestCardBlock(10L, "testuser");

            assertEquals(CardStatus.BLOCKED, cardFrom.getStatus());
            verify(cardRepository, times(1)).save(cardFrom);
        }

        @Test
        @DisplayName("Should throw exception when user requests block for another user's card")
        void requestCardBlock_CardNotOwned() {
            User anotherUser = new User();
            anotherUser.setId(2L);
            cardFrom.setOwner(anotherUser);
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(cardRepository.findById(10L)).thenReturn(Optional.of(cardFrom));

            assertThrows(ResourceNotFoundException.class, () -> cardService.requestCardBlock(10L, "testuser"));
            verify(cardRepository, never()).save(any());
        }
    }
}