package com.mokujin.government.config;

import com.mokujin.government.model.exception.BusinessException;
import com.mokujin.government.model.exception.ExceptionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@ControllerAdvice
public class BusinessExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = BusinessException.class)
    ResponseEntity<ExceptionResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        return new ResponseEntity<>(new ExceptionResponse(ex, request.getRequestURI()), ex.getStatusCode());
    }
}