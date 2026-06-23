package com.github.seecret1.cardservice.exception;

public class CardAlreadyActivated extends RuntimeException {

    public CardAlreadyActivated(String message) {
        super(message);
    }

    public CardAlreadyActivated() { }
}
