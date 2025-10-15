package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final CardService cardService;

    // @PreAuthorize("#userId == authentication.principal.id or hasRole('USER')")
    @GetMapping("/{userId}/cards")
    public ResponseEntity<Page<CardDto>> getUserCards(
            @PathVariable Long userId,
            @RequestParam(required = false) CardStatus status,
            @RequestParam(required = false, name = "search") String panLast4,
            Pageable pageable
    ) {
        Page<CardDto> cards = cardService.getCardsByUserId(userId, status, panLast4, pageable);
        return ResponseEntity.ok(cards);
    }
}