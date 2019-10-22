package com.mokujin.user.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class ProcessedUserCredentials {

    private String publicKey;

    private String privateKey;

}
