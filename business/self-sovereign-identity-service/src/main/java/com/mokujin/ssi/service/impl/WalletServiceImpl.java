package com.mokujin.ssi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mokujin.ssi.model.exception.LedgerException;
import com.mokujin.ssi.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.springframework.stereotype.Service;

import static java.util.Optional.ofNullable;
import static org.hyperledger.indy.sdk.wallet.Wallet.createWallet;
import static org.hyperledger.indy.sdk.wallet.Wallet.openWallet;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    public Wallet getOrCreateWallet(String publicKey, String privateKey) {

        ObjectNode config = objectMapper.createObjectNode();
        config.put("id", publicKey);
        ObjectNode credentials = objectMapper.createObjectNode();
        credentials.put("key", privateKey);
        try {
            return openWallet(config.toString(), credentials.toString()).get();
        } catch (Exception e) {
            log.error("Exception was thrown: " + e);
            createWallet(config.toString(), credentials.toString()).get();
            return openWallet(config.toString(), credentials.toString()).get();
        }
    }

    @Override
    @SneakyThrows
    public boolean doesWalletExist(String publicKey, String privateKey) {

        ObjectNode config = objectMapper.createObjectNode();
        config.put("id", publicKey);
        ObjectNode credentials = objectMapper.createObjectNode();
        credentials.put("key", privateKey);

        try {
            Wallet wallet = openWallet(config.toString(), credentials.toString()).get();
            wallet.closeWallet();
            return true;
        } catch (Exception e) {
            log.error("Exception was thrown: " + e);
            return false;
        }
    }
}
