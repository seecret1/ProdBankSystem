package com.github.seecret1.order_service.exception;

public class OrderValidException extends RuntimeException {

    public OrderValidException(String message, Object... args) {
        super(String.format(message, args));
    }
}
