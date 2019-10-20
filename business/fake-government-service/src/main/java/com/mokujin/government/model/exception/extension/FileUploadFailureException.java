package com.mokujin.government.model.exception.extension;

import com.mokujin.government.model.exception.BusinessException;

public class FileUploadFailureException extends BusinessException {

    public FileUploadFailureException() {
        super("Failed to save file");
    }
}
