package com.example.bankcards.util.mapper;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface CardMapper {
    @Mapping(source = "panLast4", target = "maskedPan", qualifiedByName = "formatMaskedPan")
    @Mapping(source = "status", target = "status")
    CardDto toDto(Card card);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "encryptedPan", ignore = true)
    @Mapping(target = "panLast4", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Card toEntity(CreateCardRequest request);

    @Named("formatMaskedPan")
    default String formatMaskedPan(String panLast4) {
        if (panLast4 == null || panLast4.length() != 4) {
            return "**** **** **** ****";
        }
        return "**** **** **** " + panLast4;
    }
}