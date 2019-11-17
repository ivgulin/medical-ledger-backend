package com.mokujin.ssi.service;

import org.hyperledger.indy.sdk.wallet.Wallet;

public interface WalletService {

    Wallet getOrCreateWallet(String publicKey, String privateKey) throws Exception;

    boolean doesWalletExist(String publicKey, String privateKey);
}
