package com.mokujin.user.model.presentation;

import com.mokujin.user.model.document.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Proof implements Serializable {

    private String proofRequest;
    private String proofApplication;
    private String schemaConfig;
    private String credConfig;
    private Document document;
}
