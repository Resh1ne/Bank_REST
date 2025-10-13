package com.example.bankcards.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransferRequestDto {
    @NotNull(message = "Source card ID cannot be null")
    private Long fromCardId;

    @NotNull(message = "Destination card ID cannot be null")
    private Long toCardId;

    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private String currency;
}