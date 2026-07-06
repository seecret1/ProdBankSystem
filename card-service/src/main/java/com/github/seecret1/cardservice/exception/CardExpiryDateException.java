package com.github.seecret1.cardservice.exception;

public class CardExpiryDateException extends RuntimeException {

    public CardExpiryDateException(String message) {
        super(message);
    }

    public CardExpiryDateException() { }
}
