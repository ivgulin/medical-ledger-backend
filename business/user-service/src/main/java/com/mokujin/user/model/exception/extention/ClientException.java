package com.mokujin.user.model.exception.extention;

import com.mokujin.user.model.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class ClientException extends BusinessException {
    public ClientException(HttpStatus statusCode, String error) {
        super(statusCode, error);
    }
}
