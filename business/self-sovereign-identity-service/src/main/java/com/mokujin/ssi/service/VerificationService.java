package com.mokujin.ssi.service;

import com.mokujin.ssi.model.government.KnownIdentity;
import com.mokujin.ssi.model.document.Document;
import com.mokujin.ssi.model.user.request.UserRegistrationDetails;
import com.mokujin.ssi.model.verification.Affirmation;
import com.mokujin.ssi.model.verification.Proof;

public interface VerificationService {

    KnownIdentity verifyNewbie(UserRegistrationDetails details);

    Proof presentProof(String publicKey, String privateKey, Document document);

    Affirmation verifyProof(Proof proof);
}
