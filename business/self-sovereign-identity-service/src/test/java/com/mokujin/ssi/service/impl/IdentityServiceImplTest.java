package com.mokujin.ssi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mokujin.ssi.model.government.document.impl.NationalNumber;
import com.mokujin.ssi.model.internal.Contact;
import com.mokujin.ssi.model.internal.Credential;
import com.mokujin.ssi.model.internal.Identity;
import com.mokujin.ssi.model.internal.Pseudonym;
import com.mokujin.ssi.service.IdentityService;
import lombok.SneakyThrows;

import mockit.Mock;
import mockit.MockUp;
import org.hyperledger.indy.sdk.anoncreds.Anoncreds;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.mock;

class IdentityServiceImplTest {

    private IdentityService identityService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        identityService = new IdentityServiceImpl(objectMapper);
    }

    @Test
    void findByWallet_didsAndCredentialsExistInLedger_identityWithDidsAndCredentialsIsReturned() {

        Wallet wallet = mock(Wallet.class);

        String verinymDid = "verinym did";
        String credentialId = "some id";
        String issuer = "government";
        long registrationDate = 12345467586L;
        String nationalNumber = "1225443645";
        String schemaId = "some schema id";
        String credDefId = "some cred def id";
        String contactName = "government";
        String photo = "base64 photo";
        boolean isVerinym = false;
        String pseudonymDid = "pseudonym did";

        new MockUp<Did>() {
            @Mock
            public CompletableFuture<String> getListMyDidsWithMeta(Wallet wallet1) {
                ArrayNode contacts = objectMapper.createArrayNode();

                ObjectNode verinym = objectMapper.createObjectNode();
                verinym.put("contactName", "John Doe");
                verinym.put("photo", photo);
                verinym.put("verinym", true);

                ObjectNode pseudonym = objectMapper.createObjectNode();
                pseudonym.put("contactName", contactName);
                pseudonym.put("photo", photo);
                pseudonym.put("verinym", isVerinym);

                String verinymInString = verinym.toString();
                String pseudonymInString = pseudonym.toString();

                ObjectNode verinymData = objectMapper.createObjectNode();
                verinymData.put("did", verinymDid);
                verinymData.put("verkey", "some verkey");
                verinymData.put("tempVerkey", "some temporal verkey");
                verinymData.put("metadata", verinymInString);

                ObjectNode pseudonymData = objectMapper.createObjectNode();
                pseudonymData.put("did", pseudonymDid);
                pseudonymData.put("verkey", "some verkey");
                pseudonymData.put("tempVerkey", "some temporal verkey");
                pseudonymData.put("metadata", pseudonymInString);

                contacts.add(verinymData).add(pseudonymData);

                CompletableFuture<String> future = new CompletableFuture<>();
                future.complete(contacts.toString());

                return future;
            }
        };

        new MockUp<Anoncreds>() {
            @Mock
            public CompletableFuture<String> proverGetCredentials(Wallet wallet1, String filter) {
                ObjectNode document = objectMapper.createObjectNode();
                document.put("number", nationalNumber);
                document.put("registrationDate", registrationDate);
                document.put("issuer", issuer);
                document.put("type", "number");

                ObjectNode credential = objectMapper.createObjectNode();
                credential.put("referent", credentialId);
                credential.set("attrs", document);
                credential.put("schema_id", schemaId);
                credential.put("cred_def_id", credDefId);

                ArrayNode credentials = objectMapper.createArrayNode();
                credentials.add(credential);

                CompletableFuture<String> future = new CompletableFuture<>();
                future.complete(credentials.toString());

                return future;
            }
        };

        Identity expected = Identity.builder()
                .verinymDid(verinymDid)
                .wallet(wallet)
                .credentials(Collections.singletonList(Credential.builder()
                        .id(credentialId)
                        .document(new NationalNumber(nationalNumber, registrationDate, issuer))
                        .schemaId(schemaId)
                        .schemaDefinitionId(credDefId)
                        .build()))
                .pseudonyms(Collections.singletonList(Pseudonym.builder()
                        .contact(Contact.builder()
                                .contactName(contactName)
                                .photo(photo)
                                .isVerinym(isVerinym)
                                .build())
                        .pseudonymDid(pseudonymDid)
                        .build()))
                .build();

        Identity result = identityService.findByWallet(wallet);
        System.out.println("result = " + result);

        Assertions.assertEquals(expected, result);
    }


    @Test
    void findByWallet_didsAndCredentialsDoNotExistInLedger_identityWithoutDidsAndCredentialsIsReturned() {

        Wallet wallet = mock(Wallet.class);

        new MockUp<Did>() {
            @Mock
            public CompletableFuture<String> getListMyDidsWithMeta(Wallet wallet1) {
                ArrayNode contacts = objectMapper.createArrayNode();
                CompletableFuture<String> future = new CompletableFuture<>();
                future.complete(contacts.toString());
                return future;
            }
        };

        new MockUp<Anoncreds>() {
            @Mock
            public CompletableFuture<String> proverGetCredentials(Wallet wallet1, String filter) {
                ArrayNode credentials = objectMapper.createArrayNode();
                CompletableFuture<String> future = new CompletableFuture<>();
                future.complete(credentials.toString());
                return future;
            }
        };

        Identity expected = Identity.builder()
                .wallet(wallet)
                .credentials(Collections.emptyList())
                .pseudonyms(Collections.emptyList())
                .build();

        Identity result = identityService.findByWallet(wallet);

        Assertions.assertEquals(expected, result);
    }
}