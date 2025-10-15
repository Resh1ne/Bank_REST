package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/cards")
@RequiredArgsConstructor
public class AdminCardController {

    private final CardService cardService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDto> createCard(@Valid @RequestBody CreateCardRequest createCardRequest) {
        CardDto newCard = cardService.createCard(createCardRequest);
        return new ResponseEntity<>(newCard, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CardDto>> getAllCards(Pageable pageable) {
        return ResponseEntity.ok(cardService.getAllCards(pageable));
    }

    @PostMapping("/{cardId}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDto> blockCard(@PathVariable Long cardId) {
        return ResponseEntity.ok(cardService.blockCard(cardId));
    }

    @PostMapping("/{cardId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDto> activateCard(@PathVariable Long cardId) {
        return ResponseEntity.ok(cardService.activateCard(cardId));
    }

    @DeleteMapping("/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }
}