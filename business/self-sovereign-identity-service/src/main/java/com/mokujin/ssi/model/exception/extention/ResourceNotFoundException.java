package com.mokujin.ssi.model.exception.extention;

import com.mokujin.ssi.model.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String error) {
        super(HttpStatus.NOT_FOUND, error);
    }
}
