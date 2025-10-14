package com.example.bankcards.controller;

import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.InvalidOperationException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.exception.UserAlreadyExistsException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class ExceptionHandlerController {
    @ExceptionHandler({ResourceNotFoundException.class, UserAlreadyExistsException.class, InvalidOperationException.class, InsufficientFundsException.class})
    public ResponseEntity<Map<String, String>> handleCustomExceptions(RuntimeException ex) {
        ResponseStatus responseStatus = ex.getClass().getAnnotation(ResponseStatus.class);
        HttpStatus status = (responseStatus != null) ? responseStatus.value() : HttpStatus.INTERNAL_SERVER_ERROR;

        return new ResponseEntity<>(Map.of("error", ex.getMessage()), status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        DefaultMessageSourceResolvable::getDefaultMessage
                ));
        return new ResponseEntity<>(Map.of("errors", errors), HttpStatus.BAD_REQUEST);
    }
}