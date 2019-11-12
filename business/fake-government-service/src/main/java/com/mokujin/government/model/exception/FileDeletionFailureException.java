package com.mokujin.government.model.exception;

public class FileDeletionFailureException extends RuntimeException {
    public FileDeletionFailureException() {
        super("Failed to delete file");
    }
}
