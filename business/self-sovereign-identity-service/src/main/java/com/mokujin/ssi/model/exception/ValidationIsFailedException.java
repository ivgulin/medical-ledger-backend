package com.mokujin.ssi.model.exception;

public class ValidationIsFailedException extends RuntimeException {
    public ValidationIsFailedException(String message) {
        super(message);
    }
}
