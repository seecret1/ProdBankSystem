package com.github.seecret1.delivery_service.exception;

public class NotActiveCouriers extends RuntimeException {

    public NotActiveCouriers(String message) {
        super(message);
    }
}
