package com.mokujin.government.model.exception.extention;

import com.mokujin.government.model.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String error) {
        super(HttpStatus.NOT_FOUND, error);
    }
}
