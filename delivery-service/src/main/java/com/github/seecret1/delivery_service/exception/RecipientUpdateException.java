package com.github.seecret1.delivery_service.exception;

public class RecipientUpdateException extends RuntimeException {

    public RecipientUpdateException(String message, Object... args) {
        super(String.format(message, args));
    }
}
