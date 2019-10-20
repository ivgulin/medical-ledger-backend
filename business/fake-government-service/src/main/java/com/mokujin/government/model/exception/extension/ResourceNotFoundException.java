package com.mokujin.government.model.exception.extension;

import com.mokujin.government.model.exception.BusinessException;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
