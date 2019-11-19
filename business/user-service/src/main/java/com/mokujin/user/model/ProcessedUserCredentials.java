package com.mokujin.user.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class ProcessedUserCredentials implements Serializable {

    private String publicKey;

    private String privateKey;

}
