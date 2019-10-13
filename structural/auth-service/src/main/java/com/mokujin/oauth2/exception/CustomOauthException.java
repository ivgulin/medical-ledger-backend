package com.mokujin.oauth2.exception;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mokujin.oauth2.util.CustomOauthExceptionSerializer;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;

@JsonSerialize(using = CustomOauthExceptionSerializer.class)
public class CustomOauthException extends OAuth2Exception {
    public CustomOauthException(String msg, Throwable t) {
        super(msg, t);
    }

    public CustomOauthException(String msg) {
        super(msg);
    }
}
