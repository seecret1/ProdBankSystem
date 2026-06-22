package com.github.seecret1.cardservice.exception;

public class CardStatusUpdateException extends RuntimeException {

    public CardStatusUpdateException(String message) {
        super(message);
    }

    public CardStatusUpdateException() { }
}
