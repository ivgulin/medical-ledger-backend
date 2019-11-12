package com.mokujin.government.model.exception;

public class FileUploadFailureException extends RuntimeException {

    public FileUploadFailureException() {
        super("Failed to save file");
    }
}
