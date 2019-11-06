package com.mokujin.ssi.service;

import com.mokujin.ssi.model.Contact;
import com.mokujin.ssi.model.Identity;
import org.hyperledger.indy.sdk.wallet.Wallet;

import java.util.List;
import java.util.Optional;

public interface IdentityService {

    Identity findByWallet(Wallet wallet);

}
