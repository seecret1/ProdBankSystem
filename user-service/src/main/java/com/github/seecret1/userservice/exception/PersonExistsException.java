package com.github.seecret1.userservice.exception;

public class PersonExistsException extends RuntimeException {

    public PersonExistsException(String message) {
        super(message);
    }
}
