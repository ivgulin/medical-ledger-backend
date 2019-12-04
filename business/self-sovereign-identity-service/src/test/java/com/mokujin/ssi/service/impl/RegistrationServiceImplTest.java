package com.mokujin.ssi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mokujin.ssi.model.document.Document;
import com.mokujin.ssi.model.exception.extention.LedgerException;
import com.mokujin.ssi.model.exception.extention.ResourceNotFoundException;
import com.mokujin.ssi.model.government.KnownIdentity;
import com.mokujin.ssi.model.government.document.Certificate;
import com.mokujin.ssi.model.government.document.Diploma;
import com.mokujin.ssi.model.government.document.NationalNumber;
import com.mokujin.ssi.model.government.document.NationalPassport;
import com.mokujin.ssi.model.internal.Credential;
import com.mokujin.ssi.model.internal.Identity;
import com.mokujin.ssi.model.internal.Schema;
import com.mokujin.ssi.model.user.request.UserRegistrationDetails;
import com.mokujin.ssi.model.user.response.User;
import com.mokujin.ssi.service.*;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.mokujin.ssi.model.internal.Role.DOCTOR;
import static com.mokujin.ssi.model.internal.Role.PATIENT;
import static org.hyperledger.indy.sdk.did.DidResults.CreateAndStoreMyDidResult;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class RegistrationServiceImplTest {

    @Mock
    private VerificationService verificationService;

    @Mock
    private WalletService walletService;

    @Mock
    private IdentityService identityService;

    @Mock
    private UserService userService;

    @Mock
    private CredentialService credentialService;

    @Mock
    private Pool pool;

    private Identity government;
    private Identity steward;
    private Schema passportSchema;
    private Schema nationalNumberSchema;
    private Schema diplomaSchema;
    private Schema certificateSchema;

    private RegistrationServiceImpl registrationService;

    @BeforeEach
    void setUp() {
        Wallet wallet = mock(Wallet.class);

        government = Identity.builder()
                .wallet(wallet)
                .image("test")
                .pseudonyms(new ArrayList<>())
                .build();

        steward = Identity.builder()
                .wallet(wallet)
                .pseudonyms(new ArrayList<>())
                .build();

        passportSchema = Schema.builder()
                .schemaDefinition("passport schema definition")
                .schemaDefinitionId("passport schema definition id")
                .build();
        nationalNumberSchema = Schema.builder()
                .schemaDefinition("number schema definition")
                .schemaDefinitionId("number schema definition id")
                .build();
        diplomaSchema = Schema.builder()
                .schemaDefinition("diploma schema definition")
                .schemaDefinitionId("diploma schema definition id")
                .build();
        certificateSchema = Schema.builder()
                .schemaDefinition("certificate schema definition")
                .schemaDefinitionId("certificate schema definition id")
                .build();

        registrationService = new RegistrationServiceImpl(new ObjectMapper(), verificationService, walletService,
                identityService, userService, credentialService, government, steward, pool, passportSchema,
                nationalNumberSchema, certificateSchema, diplomaSchema);

    }

    @Test
    @SneakyThrows
    void register_identityAlreadyExists_userIsReturned() {
        UserRegistrationDetails details = new UserRegistrationDetails();
        String key = "key";

        Wallet wallet = mock(Wallet.class);
        when(walletService.getOrCreateWallet(key, key)).thenReturn(wallet);

        Identity identity = Identity.builder()
                .credentials(Collections.singletonList(Credential.builder().build()))
                .build();
        when(identityService.findByWallet(wallet)).thenReturn(identity);

        User user = User.builder().build();
        when(userService.convert(identity)).thenReturn(user);

        User result = registrationService.register(details, key, key);
        verify(wallet, times(1)).close();
        assertEquals(user, result);
    }

    @Test
    @SneakyThrows
    void register_newIdentityWithDoctorRole_newDoctorIsReturned() {
        String nationalNumberValue = "1234567890";
        String name = "John";
        String lastName = "Doe";
        long date = 1234567890L;

        UserRegistrationDetails details = new UserRegistrationDetails(nationalNumberValue, name, lastName, name, date);
        String key = "key";

        Wallet wallet = mock(Wallet.class);
        when(walletService.getOrCreateWallet(key, key)).thenReturn(wallet);

        Identity identity = Identity.builder()
                .wallet(wallet)
                .credentials(new ArrayList<>())
                .pseudonyms(new ArrayList<>())
                .build();
        when(identityService.findByWallet(wallet)).thenReturn(identity);

        String issuer = "gov";
        NationalPassport nationalPassport = new NationalPassport("number", name, lastName, name, date,
                "place", "image", "male", issuer, date);
        NationalNumber nationalNumber = new NationalNumber(nationalNumberValue, date, issuer);
        KnownIdentity knownIdentity = new KnownIdentity(DOCTOR, nationalPassport, nationalNumber, null, null);
        when(verificationService.verifyNewbie(details)).thenReturn(knownIdentity);

        CreateAndStoreMyDidResult pseudonym = mock(CreateAndStoreMyDidResult.class);
        new MockUp<Did>() {
            @mockit.Mock
            public CompletableFuture<CreateAndStoreMyDidResult> createAndStoreMyDid(Wallet wallet1, String didJson) {
                assertTrue(wallet1.equals(wallet) || wallet1.equals(government.getWallet()));
                CompletableFuture<CreateAndStoreMyDidResult> future = new CompletableFuture<>();
                future.complete(pseudonym);
                return future;
            }
        };

        registrationService = spy(registrationService);
        doNothing().when(registrationService).grandVerinym(identity, knownIdentity);
        doNothing().when(registrationService).exchangeContacts(identity, knownIdentity, pseudonym, pseudonym);
        doNothing().when(registrationService).issueCredentials(key, wallet, pseudonym, knownIdentity);

        User user = User.builder().build();
        when(userService.convert(identity)).thenReturn(user);

        User result = registrationService.register(details, key, key);
        verify(wallet, times(1)).close();
        assertEquals(user, result);
    }

    @Test
    @SneakyThrows
    void register_subMethodThrowsBusinessException_exceptionIsThrown() {
        UserRegistrationDetails details = new UserRegistrationDetails();
        String key = "key";

        Wallet wallet = mock(Wallet.class);
        when(walletService.getOrCreateWallet(key, key)).thenReturn(wallet);

        Identity identity = Identity.builder()
                .wallet(wallet)
                .credentials(new ArrayList<>())
                .pseudonyms(new ArrayList<>())
                .build();
        when(identityService.findByWallet(wallet)).thenThrow(new ResourceNotFoundException("test"));

        assertThrows(LedgerException.class, () -> registrationService.register(details, key, key));
        verify(wallet, times(1)).close();
    }

    @Test
    @SneakyThrows
    void register_subMethodThrowsException_exceptionIsThrown() {
        UserRegistrationDetails details = new UserRegistrationDetails();
        String key = "key";

        Wallet wallet = mock(Wallet.class);
        when(walletService.getOrCreateWallet(key, key)).thenReturn(wallet);

        Identity identity = Identity.builder()
                .wallet(wallet)
                .credentials(new ArrayList<>())
                .pseudonyms(new ArrayList<>())
                .build();
        when(identityService.findByWallet(wallet)).thenReturn(identity);

        KnownIdentity knownIdentity = new KnownIdentity();
        when(verificationService.verifyNewbie(details)).thenReturn(knownIdentity);

        new MockUp<Did>() {
            @mockit.Mock
            public CompletableFuture<CreateAndStoreMyDidResult> createAndStoreMyDid(Wallet wallet1, String didJson)
                    throws Exception {
                assertEquals(government.getWallet(), wallet1);
                throw new Exception();
            }
        };

        assertThrows(LedgerException.class, () -> registrationService.register(details, key, key));
        verify(wallet, times(1)).close();
    }

    @Test
    @SneakyThrows
    void grandVerinym_validInputs_exceptionIsThrown() {

        String verinymDid = "did";
        String verinymVerkey = "key";
        CreateAndStoreMyDidResult verinym = mock(CreateAndStoreMyDidResult.class);
        when(verinym.getDid()).thenReturn(verinymDid);
        when(verinym.getVerkey()).thenReturn(verinymVerkey);
        Wallet wallet = mock(Wallet.class);
        Identity identity = Identity.builder().wallet(wallet).build();

        new MockUp<Did>() {
            @mockit.Mock
            public CompletableFuture<CreateAndStoreMyDidResult> createAndStoreMyDid(Wallet wallet1, String didJson) {
                assertEquals(wallet, wallet1);

                CompletableFuture<CreateAndStoreMyDidResult> future = new CompletableFuture<>();
                future.complete(verinym);
                return future;
            }
        };

        String name = "name";
        String number = "number";
        String image = "photo";

        KnownIdentity knownIdentity = new KnownIdentity();
        NationalPassport nationalPassport = new NationalPassport();
        nationalPassport.setFirstName(name);
        nationalPassport.setLastName(name);
        nationalPassport.setFatherName(name);
        nationalPassport.setImage(image);
        knownIdentity.setNationalPassport(nationalPassport);
        NationalNumber nationalNumber = new NationalNumber();
        nationalNumber.setNumber(number);
        knownIdentity.setNationalNumber(nationalNumber);

        new MockUp<Did>() {
            @mockit.Mock
            public CompletableFuture<Void> setDidMetadata(Wallet wallet1, String did, String metadata) throws IOException {
                assertEquals(wallet, wallet1);
                assertEquals(verinymDid, did);

                ObjectMapper objectMapper = new ObjectMapper();

                ObjectNode result = (ObjectNode) objectMapper.readTree(metadata);

                ObjectNode objectNode = objectMapper.createObjectNode();
                objectNode.put("contactName", name + " " + name + " " + name);
                objectNode.put("photo", image);
                objectNode.put("nationalNumber", number);
                objectNode.put("verinym", true);
                objectNode.put("visible", false);

                assertEquals(objectNode, result);

                return CompletableFuture.runAsync(() -> log.debug("In mock."));
            }
        };

        new MockUp<Ledger>() {
            @mockit.Mock
            public CompletableFuture<String> buildNymRequest(String submitterDid, String targetDid, String verkey,
                                                             String alias, String role) {

                assertEquals(steward.getVerinymDid(), submitterDid);
                assertEquals(verinymDid, targetDid);
                assertEquals(verinymVerkey, verkey);

                CompletableFuture<String> future = new CompletableFuture<>();
                future.complete(verinymDid);
                return future;
            }

            @mockit.Mock
            public CompletableFuture<String> signAndSubmitRequest(Pool pool, Wallet wallet, String submitterDid,
                                                                  String requestJson) {
                assertEquals(steward.getWallet(), wallet);
                assertEquals(steward.getVerinymDid(), submitterDid);
                assertEquals(verinymDid, requestJson);

                CompletableFuture<String> future = new CompletableFuture<>();
                future.complete("completed");
                return future;
            }
        };

        registrationService.grandVerinym(identity, knownIdentity);
    }

    @Test
    @SneakyThrows
    void exchangeContacts_validInputs_methodIsExecuted() {

        Wallet wallet = mock(Wallet.class);
        CreateAndStoreMyDidResult governmentPseudonym = mock(CreateAndStoreMyDidResult.class);
        CreateAndStoreMyDidResult userPseudonym = mock(CreateAndStoreMyDidResult.class);
        String governmentPseudonymDid = "gov did";
        String userPseudonymDid = "user did";
        when(governmentPseudonym.getDid()).thenReturn(governmentPseudonymDid);
        when(userPseudonym.getDid()).thenReturn(userPseudonymDid);

        Identity identity = Identity.builder()
                .wallet(wallet)
                .pseudonyms(new ArrayList<>())
                .build();

        String image = "image";
        String name = "John";
        String lastName = "Doe";

        KnownIdentity knownIdentity = new KnownIdentity();
        NationalPassport nationalPassport = new NationalPassport();
        nationalPassport.setFirstName(name);
        nationalPassport.setLastName(lastName);
        nationalPassport.setFatherName(name);
        nationalPassport.setImage(image);

        NationalNumber nationalNumber = new NationalNumber("12345", 123L, "gov");

        knownIdentity.setNationalPassport(nationalPassport);
        knownIdentity.setNationalNumber(nationalNumber);


        new MockUp<Did>() {
            @mockit.Mock
            public CompletableFuture<Void> setDidMetadata(Wallet wallet, String did, String metadata) throws IOException {
                assertTrue(wallet.equals(government.getWallet()) || wallet.equals(identity.getWallet()));
                assertTrue(did.equals(governmentPseudonymDid) || did.equals(userPseudonymDid));

                log.debug("metadata = " + metadata);

                ObjectMapper objectMapper = new ObjectMapper();

                ObjectNode result = (ObjectNode) objectMapper.readTree(metadata);

                ObjectNode expectedGov = objectMapper.createObjectNode();
                expectedGov.put("contactName", "Government");
                expectedGov.put("photo", "test");
                expectedGov.set("nationalNumber", null);
                expectedGov.put("verinym", false);
                expectedGov.put("visible", false);


                ObjectNode expectedUser = objectMapper.createObjectNode();
                expectedUser.put("contactName", "Doe John John");
                expectedUser.put("photo", "image");
                expectedUser.put("nationalNumber", "12345");
                expectedUser.put("verinym", false);
                expectedUser.put("visible", false);

                String s = expectedGov.toString();
                log.debug("s = " + s);

                assertTrue(result.equals(expectedGov) || result.equals(expectedUser));

                return CompletableFuture.runAsync(() -> log.debug("In mock."));
            }
        };

        registrationService.exchangeContacts(identity, knownIdentity, governmentPseudonym, userPseudonym);
    }

    @Test
    @SneakyThrows
    void issueCredentials_validInputsOfPatient_patientGetsPassportAndNationalNumber() {

        Wallet userWallet = mock(Wallet.class);
        CreateAndStoreMyDidResult governmentPseudonym = mock(CreateAndStoreMyDidResult.class);
        String pseudonymDid = "did";
        when(governmentPseudonym.getDid()).thenReturn(pseudonymDid);
        String key = "key";

        NationalPassport nationalPassport = new NationalPassport();
        NationalNumber nationalNumber = new NationalNumber();
        KnownIdentity knownIdentity = new KnownIdentity(PATIENT, nationalPassport, nationalNumber, null, null);

        new MockUp<Anoncreds>() {
            @mockit.Mock
            public CompletableFuture<String> proverCreateMasterSecret(Wallet wallet, String masterSecretId) {
                assertEquals(userWallet, wallet);
                assertEquals(key, masterSecretId);

                CompletableFuture<String> future = new CompletableFuture<>();
                future.complete(key);
                return future;
            }
        };

        ArgumentCaptor<String> schemaDefinitionCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> schemaDefinitionIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Document> documentCaptor = ArgumentCaptor.forClass(Document.class);

        registrationService = spy(registrationService);

        registrationService.issueCredentials(key, userWallet, governmentPseudonym, knownIdentity);
        verify(credentialService, times(2)).issueCredential(any(), any(),
                anyString(), schemaDefinitionIdCaptor.capture(), schemaDefinitionCaptor.capture(),
                documentCaptor.capture(), anyString());

        assertEquals(Arrays.asList(nationalNumberSchema.getSchemaDefinition(),
                passportSchema.getSchemaDefinition()), schemaDefinitionCaptor.getAllValues());
        assertEquals(Arrays.asList(nationalNumberSchema.getSchemaDefinitionId(),
                passportSchema.getSchemaDefinitionId()), schemaDefinitionIdCaptor.getAllValues());
        assertEquals(Arrays.asList(nationalNumber, nationalPassport), documentCaptor.getAllValues());
    }

    @Test
    @SneakyThrows
    void issueCredentials_validInputsOfDoctor_doctorGetsPassportAndNationalNumberAndDiplomaAndCertificate() {

        Wallet userWallet = mock(Wallet.class);
        CreateAndStoreMyDidResult governmentPseudonym = mock(CreateAndStoreMyDidResult.class);
        String pseudonymDid = "did";
        when(governmentPseudonym.getDid()).thenReturn(pseudonymDid);
        String key = "key";

        NationalPassport nationalPassport = new NationalPassport();
        NationalNumber nationalNumber = new NationalNumber();
        Diploma diploma = new Diploma();
        List<Certificate> certificates = Collections.singletonList(new Certificate());
        KnownIdentity knownIdentity = new KnownIdentity(DOCTOR, nationalPassport, nationalNumber, diploma, certificates);

        new MockUp<Anoncreds>() {
            @mockit.Mock
            public CompletableFuture<String> proverCreateMasterSecret(Wallet wallet, String masterSecretId) {
                assertEquals(userWallet, wallet);
                assertEquals(key, masterSecretId);

                CompletableFuture<String> future = new CompletableFuture<>();
                future.complete(key);
                return future;
            }
        };

        ArgumentCaptor<String> schemaDefinitionCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> schemaDefinitionIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Document> documentCaptor = ArgumentCaptor.forClass(Document.class);

        registrationService = spy(registrationService);

        registrationService.issueCredentials(key, userWallet, governmentPseudonym, knownIdentity);
        verify(credentialService, times(4)).issueCredential(any(), any(),
                anyString(), schemaDefinitionIdCaptor.capture(), schemaDefinitionCaptor.capture(),
                documentCaptor.capture(), anyString());

        assertEquals(Arrays.asList(nationalNumberSchema.getSchemaDefinition(),
                passportSchema.getSchemaDefinition(), diplomaSchema.getSchemaDefinition(),
                certificateSchema.getSchemaDefinition()), schemaDefinitionCaptor.getAllValues());
        assertEquals(Arrays.asList(nationalNumberSchema.getSchemaDefinitionId(),
                passportSchema.getSchemaDefinitionId(), diplomaSchema.getSchemaDefinitionId(),
                certificateSchema.getSchemaDefinitionId()), schemaDefinitionIdCaptor.getAllValues());
        assertEquals(Arrays.asList(nationalNumber, nationalPassport, diploma, certificates.get(0)),
                documentCaptor.getAllValues());
    }
}
