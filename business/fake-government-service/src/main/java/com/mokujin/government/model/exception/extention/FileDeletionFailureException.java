package com.mokujin.government.model.exception.extention;

import com.mokujin.government.model.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class FileDeletionFailureException extends BusinessException {
    public FileDeletionFailureException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete file.");
    }
}
