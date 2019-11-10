package com.mokujin.ssi.model.exception;

public class UserHasRegisteredException extends RuntimeException {
    public UserHasRegisteredException(String message) {
        super(message);
    }
}
