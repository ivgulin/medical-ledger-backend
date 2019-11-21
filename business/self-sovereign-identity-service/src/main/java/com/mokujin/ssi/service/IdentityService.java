package com.mokujin.ssi.service;

import com.mokujin.ssi.model.internal.Identity;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;

import static org.hyperledger.indy.sdk.did.DidResults.CreateAndStoreMyDidResult;

public interface IdentityService {

    Identity findByWallet(Wallet wallet) throws Exception;

    void establishUserConnection(Pool pool, Identity trustAnchor, CreateAndStoreMyDidResult trustAnchorPseudonym,
                                 CreateAndStoreMyDidResult userPseudonym) throws Exception;

    void exchangeContacts(Identity doctorIdentity, Identity patientIdentity, CreateAndStoreMyDidResult patientPseudonym,
                          CreateAndStoreMyDidResult doctorPseudonym) throws Exception;

}
