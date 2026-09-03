package com.github.seecret1.delivery_service.exception.handler;

import com.github.seecret1.common.dto.ErrorResponse;
import com.github.seecret1.delivery_service.exception.DeliveryException;
import com.github.seecret1.delivery_service.exception.NotActiveCouriers;
import com.github.seecret1.delivery_service.exception.RecipientUpdateException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotActiveCouriers.class)
    public ResponseEntity<ErrorResponse> handleNotActiveCouriers(
            NotActiveCouriers ex
    ) {
        log.error("GlobalRestControllerAdvice -> NotActiveCouriers: {}", ex.getMessage());
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    @ExceptionHandler(RecipientUpdateException.class)
    public ResponseEntity<ErrorResponse> handleRecipientUpdateException(
            RecipientUpdateException ex
    ) {
        log.error("GlobalRestControllerAdvice -> RecipientUpdateException: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(DeliveryException.class)
    public ResponseEntity<ErrorResponse> handleDeliveryException(
            DeliveryException ex
    ) {
        log.error("GlobalRestControllerAdvice -> DeliveryException: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(
            EntityNotFoundException ex
    ) {
        log.error("GlobalRestControllerAdvice -> EntityNotFoundException: {}", ex.getMessage());
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
