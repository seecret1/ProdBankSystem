package com.github.seecret1.invoice_service.exception.handler;

import com.github.seecret1.common.dto.ErrorResponse;
import com.github.seecret1.invoice_service.exception.InvoiceAlreadyDeletedException;
import com.github.seecret1.invoice_service.exception.InvoiceAlreadyExistsException;
import com.github.seecret1.invoice_service.exception.InvoiceNotFoundException;
import com.github.seecret1.invoice_service.exception.OperationAlreadyDeletedException;
import com.github.seecret1.invoice_service.exception.OperationNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvoiceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(InvoiceNotFoundException ex) {
        log.warn("Invoice not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(OperationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOperationNotFound(OperationNotFoundException ex) {
        log.warn("Operation not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InvoiceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyExists(InvoiceAlreadyExistsException ex) {
        log.warn("Invoice already exists: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(InvoiceAlreadyDeletedException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyDeleted(InvoiceAlreadyDeletedException ex) {
        log.warn("Invoice already deleted: {}", ex.getMessage());
        return buildResponse(HttpStatus.GONE, ex.getMessage());
    }

    @ExceptionHandler(OperationAlreadyDeletedException.class)
    public ResponseEntity<ErrorResponse> handleOperationAlreadyDeleted(OperationAlreadyDeletedException ex) {
        log.warn("Operation already deleted: {}", ex.getMessage());
        return buildResponse(HttpStatus.GONE, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");
        log.warn("Validation failed: {}", message);
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(ErrorResponse.builder()
                        .status(status.value())
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .build());
    }
}
