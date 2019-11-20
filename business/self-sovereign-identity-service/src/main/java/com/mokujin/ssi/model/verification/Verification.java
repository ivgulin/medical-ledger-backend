package com.mokujin.ssi.model.verification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Verification {

    private String proofRequest;
    private String proofApplication;
    private String schemaConfig;
    private String credConfig;
    private String formedCredential;

}
