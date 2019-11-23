package com.mokujin.ssi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mokujin.ssi.model.exception.extention.LedgerException;
import com.mokujin.ssi.model.government.KnownIdentity;
import com.mokujin.ssi.model.government.document.Document;
import com.mokujin.ssi.model.government.document.impl.NationalNumber;
import com.mokujin.ssi.model.government.document.impl.NationalPassport;
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
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static com.mokujin.ssi.model.internal.Role.PATIENT;
import static org.hyperledger.indy.sdk.anoncreds.AnoncredsResults.IssuerCreateCredentialResult;
import static org.hyperledger.indy.sdk.anoncreds.AnoncredsResults.ProverCreateCredentialRequestResult;
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
    void register_newIdentityWithPatientRole_newPatientIsReturned() {
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
        KnownIdentity knownIdentity = new KnownIdentity(PATIENT, nationalPassport, nationalNumber, null, null);
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

    // TODO: 17.11.19 redo it
    @Test
    @SneakyThrows
    @Disabled
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
            public CompletableFuture<Void> setDidMetadata(Wallet wallet, String did, String metadata) {
                assertTrue(wallet.equals(government.getWallet()) || wallet.equals(identity.getWallet()));
                assertTrue(did.equals(governmentPseudonymDid) || did.equals(userPseudonymDid));

                String expectedGovJson = "{\"contactName\":\"Government\",\"photo\":\"photo\"" +
                        ",\"nationalNumber\":null,\"visible\":false,\"verinym\":false}";
                String expectedUserJson = "{\"contactName\":\"Doe John John\",\"photo\":\"image\"," +
                        "\"nationalNumber\":\"12345\",\"visible\":false,\"verinym\":false}";
                assertTrue(metadata.equals(expectedGovJson) || metadata.equals(expectedUserJson));

                return CompletableFuture.runAsync(() -> log.debug("In mock."));
            }
        };

        when(government.getWallet()).thenReturn(wallet);

        registrationService.exchangeContacts(identity, knownIdentity, governmentPseudonym, userPseudonym);
    }

    @Test
    @SneakyThrows
    void issueCredentials_validInputs_methodIsExecuted() {

        Wallet userWallet = mock(Wallet.class);
        CreateAndStoreMyDidResult governmentPseudonym = mock(CreateAndStoreMyDidResult.class);
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
        doNothing().when(registrationService).issueCredential(any(), any(), anyString(), anyString(), any(), anyString());

        registrationService.issueCredentials(key, userWallet, governmentPseudonym, knownIdentity);
        verify(registrationService, times(2)).issueCredential(any(), any(),
                schemaDefinitionIdCaptor.capture(), schemaDefinitionCaptor.capture(),
                documentCaptor.capture(), anyString());

        assertEquals(Arrays.asList(nationalNumberSchema.getSchemaDefinition(),
                passportSchema.getSchemaDefinition()), schemaDefinitionCaptor.getAllValues());
        assertEquals(Arrays.asList(nationalNumberSchema.getSchemaDefinitionId(),
                passportSchema.getSchemaDefinitionId()), schemaDefinitionIdCaptor.getAllValues());
        assertEquals(Arrays.asList(nationalNumber, nationalPassport), documentCaptor.getAllValues());
    }

    @Test
    @SneakyThrows
    void issueCredential_validInputs_methodIsExecuted() {

        Wallet userWallet = mock(Wallet.class);
        CreateAndStoreMyDidResult governmentPseudonym = mock(CreateAndStoreMyDidResult.class);
        String governmentPseudonymDid = "gov did";
        when(governmentPseudonym.getDid()).thenReturn(governmentPseudonymDid);

        String masterSecretId = "key";
        NationalNumber nationalNumber = new NationalNumber();
        String credential = "credential";

        ProverCreateCredentialRequestResult credentialRequestResult = mock(ProverCreateCredentialRequestResult.class);
        String credentialRequest = "cred req";
        String credentialRequestMetadata = "cred req metadata";
        when(credentialRequestResult.getCredentialRequestJson()).thenReturn(credentialRequest);
        when(credentialRequestResult.getCredentialRequestMetadataJson()).thenReturn(credentialRequestMetadata);

        IssuerCreateCredentialResult issuerCreateCredentialResult = mock(IssuerCreateCredentialResult.class);
        String credentialsResult = "creds result";
        String revocRegDelta = "delta";
        when(issuerCreateCredentialResult.getCredentialJson()).thenReturn(credentialsResult);
        when(issuerCreateCredentialResult.getRevocRegDeltaJson()).thenReturn(revocRegDelta);

        String schemaDefinitionId = nationalNumberSchema.getSchemaDefinitionId();
        String schemaDefinition = nationalNumberSchema.getSchemaDefinition();

        String credentialOffer = "offer";

        new MockUp<Anoncreds>() {
            @mockit.Mock
            public CompletableFuture<String> issuerCreateCredentialOffer(Wallet wallet, String credDefId) {
                assertEquals(government.getWallet(), wallet);
                assertEquals(schemaDefinitionId, credDefId);

                CompletableFuture<String> future = new CompletableFuture<>();
                future.complete(credentialOffer);
                return future;
            }

            @mockit.Mock
            public CompletableFuture<ProverCreateCredentialRequestResult> proverCreateCredentialReq(Wallet wallet,
                                                                                                    String proverDid,
                                                                                                    String credentialOfferJson,
                                                                                                    String credentialDefJson,
                                                                                                    String masterSecretId1) {
                assertEquals(userWallet, wallet);
                assertEquals(governmentPseudonymDid, proverDid);
                assertEquals(credentialOffer, credentialOfferJson);
                assertEquals(schemaDefinition, credentialDefJson);
                assertEquals(masterSecretId, masterSecretId1);

                CompletableFuture<ProverCreateCredentialRequestResult> future = new CompletableFuture<>();
                future.complete(credentialRequestResult);
                return future;
            }

            @mockit.Mock
            public CompletableFuture<IssuerCreateCredentialResult> issuerCreateCredential(Wallet wallet,
                                                                                          String credOfferJson,
                                                                                          String credReqJson,
                                                                                          String credValuesJson,
                                                                                          String revRegId,
                                                                                          int blobStorageReaderHandle) {
                assertEquals(government.getWallet(), wallet);
                assertEquals(credentialOffer, credOfferJson);
                assertEquals(credentialRequest, credReqJson);
                assertEquals(credential, credValuesJson);

                CompletableFuture<IssuerCreateCredentialResult> future = new CompletableFuture<>();
                future.complete(issuerCreateCredentialResult);
                return future;
            }

            @mockit.Mock
            public CompletableFuture<String> proverStoreCredential(Wallet wallet, String credId,
                                                                   String credReqMetadataJson, String credJson,
                                                                   String credDefJson, String revRegDefJson) {
                assertEquals(userWallet, wallet);
                assertEquals(credentialRequestMetadata, credReqMetadataJson);
                assertEquals(credentialsResult, credJson);
                assertEquals(schemaDefinition, credDefJson);
                assertEquals(revocRegDelta, revRegDefJson);

                CompletableFuture<String> future = new CompletableFuture<>();
                future.complete(credentialOffer);
                return future;
            }
        };

        when(credentialService.getCredential(nationalNumber)).thenReturn(credential);

        registrationService.issueCredential(userWallet, governmentPseudonym, schemaDefinitionId,
                schemaDefinition, nationalNumber, masterSecretId);
    }
}
