package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final CardService cardService;

    // @PreAuthorize("#userId == authentication.principal.id or hasRole('USER')")
    @GetMapping("/{userId}/cards")
    public ResponseEntity<Page<CardDto>> getUserCards(@PathVariable Long userId, Pageable pageable) {
        Page<CardDto> cards = cardService.getCardsByUserId(userId, pageable);
        return ResponseEntity.ok(cards);
    }
}