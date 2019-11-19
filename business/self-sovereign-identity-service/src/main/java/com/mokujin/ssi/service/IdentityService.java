package com.mokujin.ssi.service;

import com.mokujin.ssi.model.internal.Identity;
import org.hyperledger.indy.sdk.did.DidResults;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;

public interface IdentityService {

    Identity findByWallet(Wallet wallet) throws Exception;

    void establishUserConnection(Pool pool, Identity trustAnchor,
                                 DidResults.CreateAndStoreMyDidResult trustAnchorPseudonym,
                                 DidResults.CreateAndStoreMyDidResult userForTrustAnchorPseudonym) throws Exception;

    void exchangeContacts(Identity doctorIdentity, Identity patientIdentity,
                          DidResults.CreateAndStoreMyDidResult patientPseudonym,
                          DidResults.CreateAndStoreMyDidResult doctorPseudonym) throws Exception;

}
