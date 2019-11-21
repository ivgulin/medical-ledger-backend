package com.mokujin.user.model.presentation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Proof {

    private String proofRequest;
    private String proofApplication;
    private String schemaConfig;
    private String credConfig;
    private String formedCredential;

}
