package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.dto.TransactionDto;
import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.entity.enums.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CardService {
    CardDto createCard(CreateCardRequest request);

    TransactionDto transferBetweenOwnCards(String username, TransferRequestDto request);

    Page<CardDto> getCardsByUserId(Long userId, CardStatus status, String panLast4, Pageable pageable);

    Page<CardDto> getAllCards(Pageable pageable);

    CardDto blockCard(Long cardId);

    CardDto activateCard(Long cardId);

    void deleteCard(Long cardId);

    CardDto requestCardBlock(Long cardId, String username);
}