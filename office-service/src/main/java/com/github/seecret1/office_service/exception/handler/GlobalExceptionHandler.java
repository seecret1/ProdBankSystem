package com.github.seecret1.office_service.exception.handler;

import com.github.seecret1.common.dto.ErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

import static com.github.seecret1.office_service.utils.ExceptionHandlerUtils.buildResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex
    ) {
        log.error("GlobalRestControllerAdvice -> DataIntegrityViolationException: {}", ex.getMessage());
        String message;

        if (ex.getMessage() != null && ex.getMessage().contains("uk_address_city_country")) {
            message = "Address already exists in this city";
        }
        else if (ex.getMessage() != null && ex.getMessage().contains("offices_contact_phone_key")) {
            message = "Contact phone already exists";
        }
        else {
            message = "Duplicate entry detected";
        }
        return buildResponse(HttpStatus.CONFLICT, message);
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

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(
            EntityNotFoundException ex
    ) {
        log.error("GlobalRestControllerAdvice -> EntityNotFoundException: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception ex
    ) {
        log.error("GlobalRestControllerAdvice -> Exception: {}", ex.getMessage());
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }
}
