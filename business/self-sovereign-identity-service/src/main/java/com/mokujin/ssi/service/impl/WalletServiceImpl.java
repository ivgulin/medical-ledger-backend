package com.mokujin.ssi.service.impl;

import com.mokujin.ssi.model.exception.LedgerException;
import com.mokujin.ssi.service.WalletService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.springframework.stereotype.Service;

import static java.util.Optional.ofNullable;
import static org.hyperledger.indy.sdk.wallet.Wallet.createWallet;
import static org.hyperledger.indy.sdk.wallet.Wallet.openWallet;

@Slf4j
@Service
public class WalletServiceImpl implements WalletService {

    @Override
    @SneakyThrows
    public Wallet getOrCreateWallet(String config, String credentials) {
        try {
            return openWallet(config, credentials).get();
        } catch (Exception e) {
            log.error("Exception was thrown: '{}'", e);
            createWallet(config, credentials).get();
            return ofNullable(openWallet(config, credentials).get())
                    .orElseThrow(() -> new LedgerException("Unable to create wallet."));
        }
    }

    @Override
    @SneakyThrows
    public boolean doesWalletExist(String config, String credentials) {
        try {
            Wallet wallet = openWallet(config, credentials).get();
            wallet.closeWallet();
            return true;
        } catch (Exception e) {
            log.error("Exception was thrown: '{}'", e);
            return false;
        }
    }
}
