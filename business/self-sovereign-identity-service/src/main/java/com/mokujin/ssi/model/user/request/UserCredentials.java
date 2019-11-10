package com.mokujin.ssi.model.user.request;

import lombok.Builder;
import lombok.Data;

@Data
public class UserCredentials {

    private String publicKey;

    private String privateKey;

}
