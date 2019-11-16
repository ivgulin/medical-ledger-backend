package com.mokujin.user.model.exception.extention;

import com.mokujin.user.model.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class ServerException extends BusinessException {
    public ServerException(HttpStatus statusCode, String error) {
        super(statusCode, error);
    }
}
