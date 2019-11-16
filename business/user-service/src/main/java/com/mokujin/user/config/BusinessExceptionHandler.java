package com.mokujin.user.config;

import com.mokujin.user.model.exception.BusinessException;
import com.mokujin.user.model.exception.ExceptionResponse;
import com.mokujin.user.model.exception.extention.ServiceIsDownException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

    @ExceptionHandler(value = IllegalStateException.class)
    ResponseEntity<ExceptionResponse> handleIllegalStateException(IllegalStateException ex, HttpServletRequest request) {
        ServiceIsDownException serviceIsDownException = new ServiceIsDownException(HttpStatus.BAD_GATEWAY, ex.getMessage());
        return new ResponseEntity<>(new ExceptionResponse(serviceIsDownException,
                request.getRequestURI()), serviceIsDownException.getStatusCode());
    }
}