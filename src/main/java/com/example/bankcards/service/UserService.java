package com.example.bankcards.service;

import com.example.bankcards.dto.RegistrationRequestDto;
import com.example.bankcards.dto.UserDto;

public interface UserService {
    UserDto registerNewUser(RegistrationRequestDto requestDto);
}