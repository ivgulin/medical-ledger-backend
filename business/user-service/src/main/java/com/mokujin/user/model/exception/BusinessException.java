package com.mokujin.user.model.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class BusinessException extends RuntimeException {

    private HttpStatus statusCode;

    public BusinessException(HttpStatus statusCode, String error) {
        super(error);
        this.statusCode = statusCode;
    }
}