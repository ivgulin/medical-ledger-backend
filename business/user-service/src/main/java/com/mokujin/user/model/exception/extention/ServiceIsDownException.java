package com.mokujin.user.model.exception.extention;

import com.mokujin.user.model.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class ServiceIsDownException extends BusinessException {
    public ServiceIsDownException(HttpStatus statusCode, String error) {
        super(statusCode, error);
    }
}
