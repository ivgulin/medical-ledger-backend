package com.mokujin.ssi.service;

import com.mokujin.ssi.model.internal.Identity;
import org.hyperledger.indy.sdk.wallet.Wallet;

public interface IdentityService {

    Identity findByWallet(Wallet wallet);

}
