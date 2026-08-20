package com.github.seecret1.userservice.exception.handler;

import com.github.seecret1.common.dto.ErrorResponse;
import com.github.seecret1.userservice.exception.*;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(
            AuthException ex
    ) {
        log.error("GlobalRestControllerAdvice -> AuthException: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(CheckPasswordException.class)
    public ResponseEntity<ErrorResponse> handleCheckPasswordException(
            CheckPasswordException ex
    ) {
        log.error("GlobalRestControllerAdvice -> CheckPasswordException: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(PasswordUpdateException.class)
    public ResponseEntity<ErrorResponse> handlePasswordUpdateException(
            PasswordUpdateException ex
    ) {
        log.error("GlobalRestControllerAdvice -> PasswordUpdateException: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(PersonException.class)
    public ResponseEntity<ErrorResponse> handlePersonException(
            PersonException ex
    ) {
        log.error("GlobalRestControllerAdvice -> PersonException: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(PersonDataExistsException.class)
    public ResponseEntity<ErrorResponse> handleIndividualDataExistsException(
            PersonDataExistsException ex
    ) {
        log.error("GlobalRestControllerAdvice -> IndividualDataExistsException: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(RegisterUserException.class)
    public ResponseEntity<ErrorResponse> handleEmailExistsException(
            RegisterUserException ex
    ) {
        log.error("GlobalRestControllerAdvice -> RegisterUserException: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(
            UserNotFoundException ex
    ) {
        log.error("GlobalRestControllerAdvice -> UserNotFoundException: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex
    ) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        });

        log.error("GlobalRestControllerAdvice -> ConstraintViolationException: {}", errors);
        return buildResponse(HttpStatus.BAD_REQUEST, errors.toString());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex
    ) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.error("GlobalRestControllerAdvice -> MethodArgumentNotValidException: {}", errors);
        return buildResponse(HttpStatus.BAD_REQUEST, errors.toString());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex
    ) {
        log.error("GlobalRestControllerAdvice -> HttpMessageNotReadableException: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid request body format");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex
    ) {
        log.error("GlobalRestControllerAdvice -> AccessDeniedException: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "Access denied: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception ex
    ) {
        log.error("GlobalRestControllerAdvice -> Exception: {}", ex.getMessage());
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String message
    ) {
        return ResponseEntity
                .status(status)
                .body(ErrorResponse
                        .builder()
                        .status(status.value())
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .build()
                );
    }
}
