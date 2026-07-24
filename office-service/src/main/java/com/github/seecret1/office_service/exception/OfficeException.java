package com.github.seecret1.office_service.exception;

public class OfficeException extends RuntimeException {

    public OfficeException(String message, Object... args) {
        super(String.format(message, args));
    }
}
