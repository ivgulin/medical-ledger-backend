package com.mokujin.ssi.service;

import com.mokujin.ssi.model.government.document.Document;
import com.mokujin.ssi.model.internal.Schema;

public interface CredentialService {

    String getCredential(Document document);

    String getProofRequest(Schema schema, Document nationalDocument);

    String getProofResponse(String proofRequest, String suitableCredential);

    String getFormedCredential(String primaryCredential);

}
