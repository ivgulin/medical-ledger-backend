package com.mokujin.common.exception.extension;

import com.mokujin.common.exception.BusinessException;

public class FileDeletionFailureException extends BusinessException {
    public FileDeletionFailureException() {
        super("Failed to delete file");
    }
}
