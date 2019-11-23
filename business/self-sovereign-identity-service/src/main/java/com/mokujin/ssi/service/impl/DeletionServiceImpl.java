package com.mokujin.ssi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mokujin.ssi.model.exception.extention.ResourceNotFoundException;
import com.mokujin.ssi.model.user.response.Auth;
import com.mokujin.ssi.service.DeletionService;
import com.mokujin.ssi.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static org.hyperledger.indy.sdk.wallet.Wallet.deleteWallet;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeletionServiceImpl implements DeletionService {

    private final ObjectMapper objectMapper;
    private final WalletService walletService;

    @Override
    @SneakyThrows
    public void delete(String publicKey, String privateKey) {

        Auth auth = walletService.doesWalletExist(publicKey, privateKey);
        if (!auth.isExists()) {
            throw new ResourceNotFoundException("No wallet was found for this user");
        }

        ObjectNode config = objectMapper.createObjectNode();
        config.put("id", publicKey);
        ObjectNode credentials = objectMapper.createObjectNode();
        credentials.put("key", privateKey);
        deleteWallet(config.toString(), credentials.toString()).get();
    }
}
