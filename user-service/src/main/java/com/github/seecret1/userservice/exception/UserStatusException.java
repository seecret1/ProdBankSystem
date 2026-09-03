package com.github.seecret1.userservice.exception;

public class UserStatusException extends RuntimeException {

    public UserStatusException(String message, Object... args) {
        super(String.format(message, args));
    }
}
