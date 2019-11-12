package com.mokujin.user.model;

import com.mokujin.user.model.document.Document;
import lombok.Data;

@Data
public class Credential {

    private String id;

    private Document document;

    private String schemaId;

    private String schemaCredentialId;

}
