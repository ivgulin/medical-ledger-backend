package com.mokujin.ssi.service;

import com.mokujin.ssi.model.government.Document;

public interface CredentialService {

    String getCredential(Document document);

/*    String getProofRequest(Schema schema);

    String getFormedCredential(String primaryCredential);

    String getProofResponse(String suitableCredential);*/
}
