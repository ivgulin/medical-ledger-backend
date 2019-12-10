package com.mokujin.ssi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mokujin.ssi.model.document.Document;
import com.mokujin.ssi.model.exception.extention.LedgerException;
import com.mokujin.ssi.model.government.KnownIdentity;
import com.mokujin.ssi.model.government.document.Certificate;
import com.mokujin.ssi.model.government.document.Diploma;
import com.mokujin.ssi.model.government.document.NationalNumber;
import com.mokujin.ssi.model.government.document.NationalPassport;
import com.mokujin.ssi.model.internal.Contact;
import com.mokujin.ssi.model.internal.Identity;
import com.mokujin.ssi.model.internal.Schema;
import com.mokujin.ssi.model.user.request.UserRegistrationDetails;
import com.mokujin.ssi.model.verification.Affirmation;
import com.mokujin.ssi.model.verification.Proof;
import com.mokujin.ssi.service.CredentialService;
import com.mokujin.ssi.service.WalletService;
import lombok.SneakyThrows;
import mockit.MockUp;
import org.hyperledger.indy.sdk.anoncreds.Anoncreds;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificationServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CredentialService credentialService;

    @Mock
    private WalletService walletService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Identity government;

    @Mock
    private Schema passportSchema;

    @Mock
    private Schema nationalNumberSchema;

    @Mock
    private Schema certificateSchema;

    @Mock
    private Schema diplomaSchema;

    private VerificationServiceImpl verificationService;

    private static Stream<Arguments> provideDocuments() {

        NationalPassport nationalPassport = new NationalPassport();
        NationalNumber nationalNumber = new NationalNumber();
        Diploma diploma = new Diploma();
        Certificate certificate = new Certificate();

        return Stream.of(
                Arguments.of(nationalPassport),
                Arguments.of(nationalNumber),
                Arguments.of(diploma),
                Arguments.of(certificate),
                Arguments.of(new Document("test") {
                    @Override
                    public String getResourceType() {
                        return "test";
                    }
                })
        );
    }

    @BeforeEach
    void setUp() {
        verificationService = new VerificationServiceImpl(restTemplate, credentialService, walletService,
                objectMapper, government, passportSchema, nationalNumberSchema, certificateSchema, diplomaSchema);
    }

    @Test
    void validateNewbie_detailsAreOk_identityIsReturned() {

        KnownIdentity knownIdentity = new KnownIdentity();

        when(restTemplate.postForObject(anyString(), any(), any())).thenReturn(knownIdentity);

        KnownIdentity result = verificationService.verifyNewbie(new UserRegistrationDetails());

        assertEquals(knownIdentity, result);
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideDocuments")
    @MockitoSettings(strictness = Strictness.LENIENT)
    void presentProof(Document document) {

        String publicKey = "public";
        String privateKey = "private";

        Wallet userWallet = mock(Wallet.class);
        when(walletService.getOrCreateWallet(publicKey, privateKey)).thenReturn(userWallet);

        if (document.getResourceType().equals("test")) {
            assertThrows(LedgerException.class, () -> verificationService.presentProof(publicKey, privateKey, document));
        } else {

            String proofRequest = "proof request";
            when(credentialService.getProofRequest(any(), any())).thenReturn(proofRequest);

            String passportSchemaId = "passport schema id";
            String passportDefId = "passport def id";
            when(passportSchema.getSchemaId()).thenReturn(passportSchemaId);
            when(passportSchema.getSchema()).thenReturn("{}");
            when(passportSchema.getSchemaDefinition()).thenReturn("{}");
            when(passportSchema.getSchemaDefinitionId()).thenReturn(passportDefId);

            String numberSchemaId = "number schema id";
            String numberDefId = "number def id";
            when(nationalNumberSchema.getSchemaId()).thenReturn(numberSchemaId);
            when(nationalNumberSchema.getSchema()).thenReturn("{}");
            when(nationalNumberSchema.getSchemaDefinition()).thenReturn("{}");
            when(nationalNumberSchema.getSchemaDefinitionId()).thenReturn(numberDefId);

            String diplomaSchemaId = "diploma schema id";
            String diplomaDefId = "diploma def id";
            when(diplomaSchema.getSchemaId()).thenReturn(diplomaSchemaId);
            when(diplomaSchema.getSchema()).thenReturn("{}");
            when(diplomaSchema.getSchemaDefinition()).thenReturn("{}");
            when(diplomaSchema.getSchemaDefinitionId()).thenReturn(diplomaDefId);

            String certificateSchemaId = "certificate schema id";
            String certificateDefId = "certificate def id";
            when(certificateSchema.getSchemaId()).thenReturn(certificateSchemaId);
            when(certificateSchema.getSchema()).thenReturn("{}");
            when(certificateSchema.getSchemaDefinition()).thenReturn("{}");
            when(certificateSchema.getSchemaDefinitionId()).thenReturn(certificateDefId);

            String suitableCredential = "suitable credential";
            String proofApplication = "proof application";

            String proofResponse = "proof response";
            when(credentialService.getProofResponse(proofRequest, suitableCredential)).thenReturn(proofResponse);

            new MockUp<Anoncreds>() {
                @mockit.Mock
                public CompletableFuture<String> proverGetCredentialsForProofReq(Wallet wallet, String proofRequest) {
                    assertEquals(userWallet, wallet);

                    CompletableFuture<String> future = new CompletableFuture<>();
                    future.complete(suitableCredential);

                    return future;
                }

                @mockit.Mock
                public CompletableFuture<String> proverCreateProof(Wallet wallet, String request, String requestedCredentials,
                                                                   String masterSecret, String schemas, String credentialDefs,
                                                                   String revStates) {
                    assertEquals(userWallet, wallet);
                    assertEquals(proofRequest, request);
                    assertEquals(proofResponse, requestedCredentials);
                    assertEquals(publicKey, masterSecret);
                    assertTrue(schemas.contains(passportSchemaId) || schemas.contains(numberSchemaId)
                            || schemas.contains(diplomaSchemaId) || schemas.contains(certificateSchemaId));
                    assertTrue(credentialDefs.contains(passportDefId) || credentialDefs.contains(numberDefId)
                            || credentialDefs.contains(diplomaDefId) || credentialDefs.contains(certificateDefId));

                    CompletableFuture<String> future = new CompletableFuture<>();
                    future.complete(proofApplication);

                    return future;
                }
            };


            Proof result = verificationService.presentProof(publicKey, privateKey, document);

            assertEquals(proofRequest, result.getProofRequest());
            assertEquals(proofApplication, result.getProofApplication());
        }
    }

    @Test
    @SneakyThrows
    void presentProof_exceptionOccursInsideTryBlock_exceptionIsThrown() {
        String publicKey = "public";
        String privateKey = "private";

        when(walletService.getOrCreateWallet(publicKey, privateKey)).thenThrow(Exception.class);

        assertThrows(LedgerException.class, () -> verificationService.presentProof(publicKey, privateKey, null));
    }

    @Test
    void verifyProof_proofIsOk_affirmationIsReturned() {

        new MockUp<Anoncreds>() {
            @mockit.Mock
            public CompletableFuture<Boolean> verifierVerifyProof(String proofRequest, String proof, String schemas,
                                                                  String credentialDefs, String revocRegDefs,
                                                                  String revocRegs) {

                CompletableFuture<Boolean> future = new CompletableFuture<>();
                future.complete(true);

                return future;
            }
        };

        when(government.getImage()).thenReturn("image");

        Affirmation expected = Affirmation.builder()
                .result(true)
                .issuedBy(Contact.builder()
                        .contactName("Government")
                        .photo("image")
                        .isVisible(true)
                        .build())
                .build();

        Affirmation result = verificationService.verifyProof(Proof.builder().build());

        assertEquals(expected, result);
    }

    @Test
    void verifyProof_exceptionOccursInsideTryBlock_exceptionIsThrown() {

        new MockUp<Anoncreds>() {
            @mockit.Mock
            public CompletableFuture<Boolean> verifierVerifyProof(String proofRequest, String proof, String schemas,
                                                                  String credentialDefs, String revocRegDefs,
                                                                  String revocRegs) throws Exception {

                throw new Exception();
            }
        };

        assertThrows(LedgerException.class, () -> verificationService.verifyProof(Proof.builder().build()));
    }
}