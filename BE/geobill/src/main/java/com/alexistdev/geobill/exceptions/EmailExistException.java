package com.alexistdev.geobill.exceptions;

public class EmailExistException extends RuntimeException {
    public EmailExistException(String message) {
        super(message);
    }
}
