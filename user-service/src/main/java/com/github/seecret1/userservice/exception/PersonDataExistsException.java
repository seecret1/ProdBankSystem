package com.github.seecret1.userservice.exception;

public class PersonDataExistsException extends RuntimeException {

    public PersonDataExistsException(String message) {
        super(message);
    }
}
