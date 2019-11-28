package com.mokujin.ssi.service;

import com.mokujin.ssi.model.document.Document;
import com.mokujin.ssi.model.internal.Schema;
import com.mokujin.ssi.model.user.request.OfferRequest;
import com.mokujin.ssi.model.user.response.User;
import org.hyperledger.indy.sdk.wallet.Wallet;

public interface CredentialService {

    String getCredential(Document document);

    String getProofRequest(Schema schema, Document nationalDocument);

    String getProofResponse(String proofRequest, String suitableCredential);

    User addCredential(String publicKey, String privateKey, OfferRequest offerRequest);

    void issueCredential(Wallet userWallet, Wallet trustAnchorWallet, String trustAnchorPseudonym,
                         String schemaDefinitionId, String schemaDefinition, Document document, String masterSecretId) throws Exception;

}
