package com.mokujin.user.service;

import com.mokujin.user.model.document.Document;
import com.mokujin.user.model.presentation.Affirmation;
import com.mokujin.user.model.presentation.PresentationAttributes;
import com.mokujin.user.model.presentation.PresentationRequest;
import com.mokujin.user.model.presentation.Proof;

import static com.mokujin.user.model.User.Role;

public interface PresentationService {

    PresentationAttributes getPresentationAttributes(Role role);

    void requestPresentation(String publicKey, String privateKey, PresentationRequest presentationRequest,
                             String connectionNumber);

    void presentProof(String publicKey, String privateKey, Document document, String connectionNumber);

    Affirmation verifyProof(Proof proof, String nationalNumber, String connectionNumber);
}
