package com.github.seecret1.userservice.exception;

public class AuthException extends RuntimeException {

    public AuthException() {}

    public AuthException(String message) {
        super(message);
    }
}
