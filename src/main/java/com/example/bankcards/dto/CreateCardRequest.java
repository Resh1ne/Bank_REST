package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCardRequest {
    @NotNull(message = "Owner ID cannot be null")
    private Long ownerId;

    @NotBlank(message = "Holder name cannot be blank")
    @Size(min = 2, max = 100, message = "Holder name must be between 2 and 100 characters")
    private String holderName;

    // Регулярное выражение для формата MM/YYYY
    @NotBlank(message = "Expiry date cannot be blank")
    @Pattern(regexp = "^(0[1-9]|1[0-2])\\/(\\d{4})$", message = "Expiry date must be in MM/YYYY format")
    private String expiryDate;
}