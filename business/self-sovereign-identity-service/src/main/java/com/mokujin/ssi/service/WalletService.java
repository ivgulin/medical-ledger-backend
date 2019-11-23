package com.mokujin.ssi.service;

import com.mokujin.ssi.model.user.response.Auth;
import org.hyperledger.indy.sdk.wallet.Wallet;

public interface WalletService {

    Wallet getOrCreateWallet(String publicKey, String privateKey) throws Exception;

    Auth doesWalletExist(String publicKey, String privateKey);
}
