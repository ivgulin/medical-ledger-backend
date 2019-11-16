package com.mokujin.ssi.config;

import com.mokujin.ssi.model.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@ControllerAdvice
public class BusinessExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = BusinessException.class)
    ResponseEntity<BusinessException> handleBusinessException(BusinessException ex) {
        return new ResponseEntity<>(ex, ex.getStatusCode());
    }

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<BusinessException> handleException(Exception ex) {
        return new ResponseEntity<>(new BusinessException(INTERNAL_SERVER_ERROR, ex.getMessage()), INTERNAL_SERVER_ERROR);
    }
}