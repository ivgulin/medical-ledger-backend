package com.mokujin.ssi.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mokujin.ssi.model.internal.Credential;
import com.mokujin.ssi.model.internal.DidWithMetadata;
import com.mokujin.ssi.model.internal.Identity;
import com.mokujin.ssi.model.internal.Pseudonym;
import com.mokujin.ssi.service.IdentityService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.indy.sdk.anoncreds.Anoncreds;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.hyperledger.indy.sdk.anoncreds.Anoncreds.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdentityServiceImpl implements IdentityService {

    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    public Identity findByWallet(Wallet wallet) {

        Identity identity = new Identity();
        identity.setWallet(wallet);

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
                .ifPresent(didWithMetadata -> identity.setVerinymDid(didWithMetadata.getDid()));

        List<Pseudonym> pseudonyms = didsWithMetadata.stream()
                .filter(d -> !d.getMetadata().isVerinym())
                .map(d -> Pseudonym.builder()
                        .pseudonymDid(d.getDid())
                        .contact(d.getMetadata())
                        .build()).collect(Collectors.toList());
        identity.setPseudonyms(pseudonyms);

        String credentials = proverGetCredentials(wallet, "{}").get();
        log.info("'credentials={}'", credentials);

        List<Credential> credentialList = credentials.equals("[]")
                ? new ArrayList<>()
                : objectMapper.readValue(credentials, new TypeReference<List<Credential>>() {
        });
        identity.setCredentials(credentialList);

        return identity;
    }
}
