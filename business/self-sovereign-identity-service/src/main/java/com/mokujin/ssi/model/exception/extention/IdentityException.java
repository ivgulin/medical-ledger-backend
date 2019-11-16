package com.mokujin.ssi.model.exception.extention;

import com.mokujin.ssi.model.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class IdentityException extends BusinessException {

    public IdentityException(HttpStatus statusCode, String error) {
        super(statusCode, error);
    }
}
