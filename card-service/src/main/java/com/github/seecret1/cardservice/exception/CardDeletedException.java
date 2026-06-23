package com.github.seecret1.cardservice.exception;

public class CardDeletedException extends RuntimeException {

    public CardDeletedException(String message) {
        super(message);
    }

    public CardDeletedException() { }
}
