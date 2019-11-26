package com.mokujin.ssi.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mokujin.ssi.model.internal.DidWithMetadata;
import com.mokujin.ssi.model.user.response.Auth;
import com.mokujin.ssi.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.mokujin.ssi.model.internal.Role.DOCTOR;
import static org.hyperledger.indy.sdk.wallet.Wallet.createWallet;
import static org.hyperledger.indy.sdk.wallet.Wallet.openWallet;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final ObjectMapper objectMapper;

    @Override
    public Wallet getOrCreateWallet(String publicKey, String privateKey) throws Exception {

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
    public Auth doesWalletExist(String publicKey, String privateKey) {

        ObjectNode config = objectMapper.createObjectNode();
        config.put("id", publicKey);
        ObjectNode credentials = objectMapper.createObjectNode();
        credentials.put("key", privateKey);

        try (Wallet wallet = openWallet(config.toString(), credentials.toString()).get()) {

            Auth auth = new Auth(true, null);
            String listOfDids = Did.getListMyDidsWithMeta(wallet).get()
                    .replace("\\", "")
                    .replace("\"{", "{")
                    .replace("}\"", "}");
            log.info("'listOfDids={}'", listOfDids);

            List<DidWithMetadata> didsWithMetadata = objectMapper.readValue(listOfDids,
                    new TypeReference<List<DidWithMetadata>>() {
                    });
            log.info("'didsWithMetadata={}'", didsWithMetadata);
            didsWithMetadata.stream()
                    .filter(d -> d.getMetadata().isVerinym())
                    .findAny()
                    .ifPresent(didWithMetadata -> {
                        auth.setRole(DOCTOR);
                    });

            return auth;
        } catch (Exception e) {
            log.error("Exception was thrown: " + e);
            return new Auth(false, null);
        }
    }
}
