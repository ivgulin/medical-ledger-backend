package com.mokujin.common.exception.extension;

import com.mokujin.common.exception.BusinessException;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
