package com.github.seecret1.cardservice.exception;

public class CardExistsException extends RuntimeException{

    public CardExistsException(String message) {
        super(message);
    }
}
