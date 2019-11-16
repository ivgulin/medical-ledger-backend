package com.mokujin.ssi.model.exception.extention;

import com.mokujin.ssi.model.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class LedgerException extends BusinessException {
    public LedgerException(HttpStatus statusCode, String error) {
        super(statusCode, error);
    }
}
