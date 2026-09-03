package com.github.seecret1.invoice_service.exception;

public class InvoiceAlreadyDeletedException extends RuntimeException {

    public InvoiceAlreadyDeletedException(String message) {
        super(message);
    }
}
