package com.github.seecret1.invoice_service.exception;

public class OperationAlreadyDeletedException extends RuntimeException {
    public OperationAlreadyDeletedException(String message) {
        super(message);
    }
}
