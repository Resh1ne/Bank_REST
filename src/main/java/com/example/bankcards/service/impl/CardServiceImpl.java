package com.example.bankcards.service.impl;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.dto.TransactionDto;
import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.TransactionStatus;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.InvalidOperationException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.util.mapper.CardMapper;
import com.example.bankcards.util.mapper.TransactionMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final CardMapper cardMapper;
    private final TransactionMapper transactionMapper;

    @Override
    @Transactional
    public CardDto createCard(CreateCardRequest request) {
        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + request.getOwnerId() + " not found."));

        Card card = cardMapper.toEntity(request);
        card.setOwner(owner);

        String plainPan = generateDummyPan();
        card.setPan(plainPan);
        card.setPanLast4(plainPan.substring(plainPan.length() - 4));

        Card savedCard = cardRepository.save(card);
        return cardMapper.toDto(savedCard);
    }

    @Override
    public Page<CardDto> getCardsByUserId(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User with id " + userId + " not found.");
        }
        User user = new User();
        user.setId(userId);

        Page<Card> cardsPage = cardRepository.findByOwner(user, pageable);
        return cardsPage.map(cardMapper::toDto);
    }

    @Override
    @Transactional
    public TransactionDto transferBetweenOwnCards(Long userId, TransferRequestDto request) {
        if (Objects.equals(request.getFromCardId(), request.getToCardId())) {
            throw new InvalidOperationException("Source and destination cards cannot be the same.");
        }

        Card fromCard = findCardByIdAndUserId(request.getFromCardId(), userId);
        Card toCard = findCardByIdAndUserId(request.getToCardId(), userId);
        BigDecimal amount = request.getAmount();

        validateCardForTransfer(fromCard, amount);
        validateCardForTransfer(toCard, null);

        fromCard.setBalance(fromCard.getBalance().subtract(request.getAmount()));
        toCard.setBalance(toCard.getBalance().add(request.getAmount()));

        Transaction transaction = logTransaction(fromCard, toCard, request.getAmount(), TransactionStatus.COMPLETED, "Transfer between own cards");

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
        return transactionMapper.toDto(transaction);
    }

    private Card findCardByIdAndUserId(Long cardId, Long userId) {
        return cardRepository.findById(cardId)
                .filter(card -> card.getOwner().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Card with id " + cardId + " not found or does not belong to user " + userId));
    }

    private void validateCardForTransfer(Card card, BigDecimal amountToWithdraw) {
        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new InvalidOperationException("Card " + card.getId() + " is not active. Current status: " + card.getStatus());
        }
        if (amountToWithdraw != null && card.getBalance().compareTo(amountToWithdraw) < 0) {
            throw new InsufficientFundsException("Insufficient funds on card " + card.getId());
        }
    }

    private Transaction logTransaction(Card from, Card to, BigDecimal amount, TransactionStatus status, String description) {
        Transaction transaction = new Transaction();
        transaction.setCardFrom(from);
        transaction.setCardTo(to);
        transaction.setAmount(amount);
        transaction.setStatus(status);
        transaction.setDescription(description);
        transaction.setCurrency("BYN");
        return transactionRepository.save(transaction);
    }

    private String generateDummyPan() {
        return UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 16);
    }
}