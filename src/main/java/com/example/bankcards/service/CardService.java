package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.dto.TransactionDto;
import com.example.bankcards.dto.TransferRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CardService {
    CardDto createCard(CreateCardRequest request);

    TransactionDto transferBetweenOwnCards(Long userId, TransferRequestDto request);

    Page<CardDto> getCardsByUserId(Long userId, Pageable pageable);
}