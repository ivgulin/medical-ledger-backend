package com.mokujin.ssi.service;

import org.hyperledger.indy.sdk.wallet.Wallet;

public interface WalletService {

    Wallet getOrCreateWallet(String config, String credentials);

    boolean doesWalletExist(String config, String credentials);
}
