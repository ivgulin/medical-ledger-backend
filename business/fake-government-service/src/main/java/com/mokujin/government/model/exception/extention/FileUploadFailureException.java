package com.mokujin.government.model.exception.extention;

import com.mokujin.government.model.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class FileUploadFailureException extends BusinessException {
    public FileUploadFailureException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save file.");

    }
}
