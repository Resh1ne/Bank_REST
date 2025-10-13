package com.example.bankcards.mapper;

import com.example.bankcards.dto.TransactionDto;
import com.example.bankcards.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(source = "cardFrom.id", target = "cardFromId")
    @Mapping(source = "cardTo.id", target = "cardToId")
    TransactionDto toDto(Transaction transaction);
}