package com.example.bankcards.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CardDto {
    private Long id;
    private String maskedPan;
    private String holderName;
    private String expiryDate;
    private String status;
    private BigDecimal balance;
}