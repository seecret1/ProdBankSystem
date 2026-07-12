package com.github.seecret1.order_service.exception;

public class OrderCardCreationException extends RuntimeException {

    public OrderCardCreationException(String message) {
        super(message);
    }

    public OrderCardCreationException() {}
}
