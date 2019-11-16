package com.mokujin.government.model.exception;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class ExceptionResponse {

    private String timestamp;

    private int status;

    private String error;

    private String message;

    private String path;

    public ExceptionResponse(BusinessException ex, String path) {
        this.timestamp = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now());
        this.status = ex.getStatusCode().value();
        this.error = ex.getStatusCode().getReasonPhrase();
        this.message = ex.getMessage();
        this.path = path;
    }
}