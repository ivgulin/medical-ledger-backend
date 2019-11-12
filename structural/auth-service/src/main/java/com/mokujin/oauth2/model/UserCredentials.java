package com.mokujin.oauth2.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserCredentials {

    private String publicKey;

    private String privateKey;

}
