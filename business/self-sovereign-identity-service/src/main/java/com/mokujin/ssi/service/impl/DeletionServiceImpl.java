package com.mokujin.ssi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mokujin.ssi.model.exception.ResourceNotFoundException;
import com.mokujin.ssi.model.user.request.UserCredentials;
import com.mokujin.ssi.service.DeletionService;
import com.mokujin.ssi.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.springframework.stereotype.Service;

import static org.hyperledger.indy.sdk.wallet.Wallet.deleteWallet;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeletionServiceImpl implements DeletionService {

    private final ObjectMapper objectMapper;
    private final WalletService walletService;

    @Override
    public void delete(UserCredentials userCredentials) {

        try {
            ObjectNode config = objectMapper.createObjectNode();
            config.put("id", userCredentials.getPublicKey());
            ObjectNode credentials = objectMapper.createObjectNode();
            credentials.put("key", userCredentials.getPrivateKey());

            if (!walletService.doesWalletExist(config.toString(), credentials.toString())) {
                throw new ResourceNotFoundException("No wallet was found for this user");
            }

            Wallet userWallet = walletService
                    .getOrCreateWallet(userCredentials.getPublicKey(), userCredentials.getPrivateKey());
            userWallet.close();
            deleteWallet(config.toString(), credentials.toString()).get();

        } catch (Exception e) {
            log.error("Exception was thrown: " + e);
            e.printStackTrace();
        }
    }
}
