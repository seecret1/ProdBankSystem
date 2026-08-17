package com.github.seecret1.delivery_service.exception;

public class DeliveryException extends RuntimeException {

    public DeliveryException(String message, Object... args) {
        super(String.format(message, args));
    }
}
