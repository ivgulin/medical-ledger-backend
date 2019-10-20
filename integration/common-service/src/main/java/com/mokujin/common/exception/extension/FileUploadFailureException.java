package com.mokujin.common.exception.extension;

import com.mokujin.common.exception.BusinessException;

public class FileUploadFailureException extends BusinessException {

    public FileUploadFailureException() {
        super("Failed to save file");
    }
}
