package com.github.seecret1.cardservice.exception;

public class CardDeletedException extends RuntimeException {

    public CardDeletedException(String message, Object... args) {
        super(String.format(message, args));
    }
}
