package com.mokujin.ssi.service;

import com.mokujin.ssi.model.government.document.impl.NationalNumber;
import com.mokujin.ssi.model.government.document.impl.NationalPassport;
import com.mokujin.ssi.model.internal.Identity;
import org.hyperledger.indy.sdk.did.DidResults;
import org.hyperledger.indy.sdk.wallet.Wallet;

public interface IdentityService {

    Identity findByWallet(Wallet wallet);

    void establishUserConnection(Identity trustAnchor,
                                 DidResults.CreateAndStoreMyDidResult trustAnchorPseudonym,
                                 DidResults.CreateAndStoreMyDidResult userForTrustAnchorPseudonym) throws Exception;

    void exchangeContacts(Identity doctorIdentity, Identity patientIdentity,
                          DidResults.CreateAndStoreMyDidResult patientPseudonym,
                          DidResults.CreateAndStoreMyDidResult doctorPseudonym) throws Exception;

}
