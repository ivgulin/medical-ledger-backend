package com.mokujin.ssi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mokujin.ssi.model.exception.extention.ResourceNotFoundException;
import com.mokujin.ssi.model.government.document.NationalNumber;
import com.mokujin.ssi.model.government.document.NationalPassport;
import com.mokujin.ssi.model.internal.Contact;
import com.mokujin.ssi.model.internal.Credential;
import com.mokujin.ssi.model.internal.Identity;
import com.mokujin.ssi.model.internal.Pseudonym;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import mockit.MockUp;
import org.hyperledger.indy.sdk.anoncreds.Anoncreds;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.ledger.Ledger;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static com.mokujin.ssi.model.internal.Role.DOCTOR;
import static com.mokujin.ssi.model.internal.Role.PATIENT;
import static org.hyperledger.indy.sdk.did.DidResults.CreateAndStoreMyDidResult;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
class IdentityServiceImplTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    private IdentityServiceImpl identityService;

    private static Stream<Arguments> provideIncompleteDocumentPack() {

        List<Credential> onlyPassportPack = new ArrayList<>();
        onlyPassportPack.add(new Credential("id", new NationalPassport(), "", ""));

        Identity onlyPassportIdentity = Identity.builder()
                .credentials(onlyPassportPack)
                .build();

        List<Credential> onlyNationalNumberPack = new ArrayList<>();
        onlyNationalNumberPack.add(new Credential("id", new NationalNumber(), "", ""));

        Identity onlyNationalNumberIdentity = Identity.builder()
                .credentials(onlyNationalNumberPack)
                .build();
        return Stream.of(Arguments.of(onlyPassportIdentity), Arguments.of(onlyNationalNumberIdentity));
    }

    @BeforeEach
    void setUp() {
        identityService = new IdentityServiceImpl(objectMapper);
    }

    @Test
    @SneakyThrows
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
            @mockit.Mock
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
            @mockit.Mock
            public CompletableFuture<String> proverGetCredentials(Wallet wallet1, String filter) {
                ObjectNode document = objectMapper.createObjectNode();
                document.put("number", nationalNumber);
                document.put("registrationDate", registrationDate);
                document.put("issuer", issuer);
                document.put("resourceType", "Number");

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
                .role(DOCTOR)
                .image(photo)
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

        assertEquals(expected, result);
    }

    @Test
    @SneakyThrows
    void findByWallet_didsAndCredentialsDoNotExistInLedger_identityWithoutDidsAndCredentialsIsReturned() {

        Wallet wallet = mock(Wallet.class);

        new MockUp<Did>() {
            @mockit.Mock
            CompletableFuture<String> getListMyDidsWithMeta(Wallet wallet1) {
                ArrayNode contacts = objectMapper.createArrayNode();
                CompletableFuture<String> future = new CompletableFuture<>();
                future.complete(contacts.toString());
                return future;
            }
        };

        new MockUp<Anoncreds>() {
            @mockit.Mock
            public CompletableFuture<String> proverGetCredentials(Wallet wallet1, String filter) {
                ArrayNode credentials = objectMapper.createArrayNode();
                CompletableFuture<String> future = new CompletableFuture<>();
                future.complete(credentials.toString());
                return future;
            }
        };

        Identity expected = Identity.builder()
                .wallet(wallet)
                .role(PATIENT)
                .credentials(Collections.emptyList())
                .pseudonyms(Collections.emptyList())
                .build();

        Identity result = identityService.findByWallet(wallet);

        assertEquals(expected, result);
    }

    @Test
    @SneakyThrows
    void establishUserConnection_validInputs_methodIsExecuted() {

        Wallet wallet = mock(Wallet.class);
        Pool pool = mock(Pool.class);

        CreateAndStoreMyDidResult trustAnchorPseudonym = mock(CreateAndStoreMyDidResult.class);
        CreateAndStoreMyDidResult userPseudonym = mock(CreateAndStoreMyDidResult.class);
        String governmentPseudonymDid = "gov did";
        String governmentPseudonymVerkey = "gov verkey";
        String userPseudonymDid = "user did";
        String userPseudonymVerkey = "user verkey";
        when(trustAnchorPseudonym.getDid()).thenReturn(governmentPseudonymDid);
        when(trustAnchorPseudonym.getVerkey()).thenReturn(governmentPseudonymVerkey);
        when(userPseudonym.getDid()).thenReturn(userPseudonymDid);
        when(userPseudonym.getVerkey()).thenReturn(userPseudonymVerkey);

        String verinymDid = "did";
        Identity identity = Identity.builder()
                .wallet(wallet)
                .verinymDid(verinymDid)
                .build();

        String responseUsingGovCreds = "response for gov";
        String responseUsingUserCreds = "response for user";
        new MockUp<Ledger>() {
            @mockit.Mock
            public CompletableFuture<String> buildNymRequest(String submitterDid, String targetDid, String verkey,
                                                             String alias, String role) {
                assertEquals(verinymDid, submitterDid);
                assertTrue(targetDid.equals(governmentPseudonymDid) || targetDid.equals(userPseudonymDid));
                assertTrue(verkey.equals(governmentPseudonymVerkey) || verkey.equals(userPseudonymVerkey));

                CompletableFuture<String> future = new CompletableFuture<>();
                if (targetDid.equals(governmentPseudonymDid) && verkey.equals(governmentPseudonymVerkey)) {
                    future.complete(responseUsingGovCreds);
                }
                if (targetDid.equals(userPseudonymDid) && verkey.equals(userPseudonymVerkey)) {
                    future.complete(responseUsingUserCreds);
                }
                return future;
            }

            @mockit.Mock
            public CompletableFuture<String> signAndSubmitRequest(Pool pool, Wallet wallet, String submitterDid,
                                                                  String requestJson) {
                assertEquals(identity.getWallet(), wallet);
                assertEquals(verinymDid, submitterDid);
                assertTrue(requestJson.equals(responseUsingGovCreds) || requestJson.equals(responseUsingUserCreds));

                CompletableFuture<String> future = new CompletableFuture<>();
                future.complete("response");
                return future;
            }
        };

        identityService.establishUserConnection(pool, identity, trustAnchorPseudonym, userPseudonym);
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideIncompleteDocumentPack")
    void exchangeContacts_incompletePackOfDocuments_exceptionIsThrown(Identity identity) {

        CreateAndStoreMyDidResult pseudonym = mock(CreateAndStoreMyDidResult.class);
        assertThrows(ResourceNotFoundException.class, () -> identityService
                .exchangeContacts(identity, identity, pseudonym, pseudonym));
    }

    @Test
    @SneakyThrows
    void exchangeContacts_validInputs_methodIsExecuted() {
        List<Credential> credentials = new ArrayList<>();
        NationalPassport passport = new NationalPassport();
        NationalNumber nationalNumber = new NationalNumber();
        credentials.add(new Credential("id", passport, "", ""));
        credentials.add(new Credential("id", nationalNumber, "", ""));

        Identity identity = Identity.builder()
                .credentials(credentials)
                .build();

        CreateAndStoreMyDidResult pseudonym = mock(CreateAndStoreMyDidResult.class);

        identityService = spy(identityService);
        doNothing().when(identityService).addContact(identity, pseudonym, passport, nationalNumber);

        identityService.exchangeContacts(identity, identity, pseudonym, pseudonym);
    }

    @Test
    @SneakyThrows
    void addContact_validInputs_mathodIsExecuted() {
        String name = "test";

        CreateAndStoreMyDidResult pseudonym = mock(CreateAndStoreMyDidResult.class);
        when(pseudonym.getDid()).thenReturn(name);

        NationalPassport passport = new NationalPassport();
        passport.setFirstName(name);
        passport.setLastName(name);
        passport.setFatherName(name);
        passport.setImage(name);

        NationalNumber nationalNumber = new NationalNumber();
        nationalNumber.setNumber(name);

        Wallet userWallet = mock(Wallet.class);
        Identity identity = new Identity();
        identity.setWallet(userWallet);

        new MockUp<Did>() {
            @mockit.Mock
            public CompletableFuture<Void> setDidMetadata(Wallet wallet, String did, String metadata) {
                assertEquals(userWallet, wallet);
                assertEquals(name, did);

                return CompletableFuture.runAsync(() -> log.debug("In mock."));
            }
        };

        identityService.addContact(identity, pseudonym, passport, nationalNumber);
    }
}
