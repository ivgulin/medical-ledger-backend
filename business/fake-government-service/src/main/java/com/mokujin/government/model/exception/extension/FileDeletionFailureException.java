package com.mokujin.government.model.exception.extension;

import com.mokujin.government.model.exception.BusinessException;

public class FileDeletionFailureException extends BusinessException {
    public FileDeletionFailureException() {
        super("Failed to delete file");
    }
}
