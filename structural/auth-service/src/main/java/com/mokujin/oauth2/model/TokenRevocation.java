package com.mokujin.oauth2.model;

import lombok.Data;

@Data
public class TokenRevocation {

    private String access_token;

    private String refresh_token;

}
