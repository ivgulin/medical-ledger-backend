package com.mokujin.ssi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mokujin.ssi.model.document.Document;
import com.mokujin.ssi.model.document.medical.dicom.MedicalImage;
import com.mokujin.ssi.model.document.medical.hl7.Procedure;
import com.mokujin.ssi.model.document.medical.hl7.component.*;
import com.mokujin.ssi.model.exception.extention.LedgerException;
import com.mokujin.ssi.model.exception.extention.ResourceNotFoundException;
import com.mokujin.ssi.model.government.document.NationalNumber;
import com.mokujin.ssi.model.government.document.NationalPassport;
import com.mokujin.ssi.model.internal.Contact;
import com.mokujin.ssi.model.internal.Identity;
import com.mokujin.ssi.model.internal.Pseudonym;
import com.mokujin.ssi.model.internal.Schema;
import com.mokujin.ssi.model.user.request.OfferRequest;
import com.mokujin.ssi.model.user.request.UserCredentials;
import com.mokujin.ssi.model.user.response.User;
import com.mokujin.ssi.service.IdentityService;
import com.mokujin.ssi.service.SchemaService;
import com.mokujin.ssi.service.UserService;
import com.mokujin.ssi.service.WalletService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import mockit.MockUp;
import org.hyperledger.indy.sdk.anoncreds.Anoncreds;
import org.hyperledger.indy.sdk.anoncreds.AnoncredsResults;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static com.mokujin.ssi.model.document.medical.hl7.component.Narrative.NarrativeStatus.generated;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class CredentialServiceImplTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SchemaService schemaService;

    @Mock
    private IdentityService identityService;

    @Mock
    private WalletService walletService;

    @Mock
    private UserService userService;

    @Mock
    private Pool pool;

    private CredentialServiceImpl credentialService;

    private static Stream<Arguments> getCredentials_provideDocumentsAndResultExpectations() {

        String nationalNumber = "1234567890";
        long someDate = 1234567890L;
        String issuer = "government";
        String name = "John";
        String lastName = "Doe";
        String placeOfBirth = "place";
        String image = "encrypted";
        String sex = "male";
        String number = "number";

        ObjectMapper objectMapper = new ObjectMapper();

        ObjectNode passportNode = getPassportNode(someDate, issuer, name, lastName, placeOfBirth,
                image, sex, objectMapper, number);

        ObjectNode nationalNumberNode = getNationalNumber(nationalNumber, someDate, issuer, objectMapper);

        ObjectNode medicalImageNode = getMedicalImage(objectMapper);
        Map<String, String> attributesMap = new HashMap<>();
        attributesMap.put("test", "test");

        return Stream.of(
                Arguments.of(new NationalPassport(number, name, lastName, name, someDate, placeOfBirth,
                        image, sex, issuer, someDate), passportNode.toString()),
                Arguments.of(new NationalNumber(nationalNumber, someDate, issuer), nationalNumberNode.toString()),
                Arguments.of(new MedicalImage(attributesMap), medicalImageNode.toString())
        );
    }

    private static ObjectNode getNationalNumber(String nationalNumber, long someDate, String issuer,
                                                ObjectMapper objectMapper) {
        ObjectNode nationalNumberNode = objectMapper.createObjectNode();

        ObjectNode attributeOne = objectMapper.createObjectNode();
        attributeOne.put("raw", nationalNumber);

        ObjectNode attributeTwo = objectMapper.createObjectNode();
        attributeTwo.put("raw", String.valueOf(someDate));

        ObjectNode attributeThree = objectMapper.createObjectNode();
        attributeThree.put("raw", issuer);

        ObjectNode attributeFour = objectMapper.createObjectNode();
        attributeFour.put("raw", "Number");

        nationalNumberNode.set("number", attributeOne);
        nationalNumberNode.set("registrationDate", attributeTwo);
        nationalNumberNode.set("issuer", attributeThree);
        nationalNumberNode.set("resourceType", attributeFour);
        return nationalNumberNode;
    }

    private static ObjectNode getPassportNode(long someDate, String issuer, String name, String lastName,
                                              String placeOfBirth, String image, String sex,
                                              ObjectMapper objectMapper, String number) {
        ObjectNode passportNode = objectMapper.createObjectNode();

        ObjectNode attributeOne = objectMapper.createObjectNode();
        attributeOne.put("raw", number);

        ObjectNode attributeTwo = objectMapper.createObjectNode();
        attributeTwo.put("raw", name);

        ObjectNode attributeThree = objectMapper.createObjectNode();
        attributeThree.put("raw", lastName);

        ObjectNode attributeFour = objectMapper.createObjectNode();
        attributeFour.put("raw", name);

        ObjectNode attributeFive = objectMapper.createObjectNode();
        attributeFive.put("raw", String.valueOf(someDate));

        ObjectNode attributeSix = objectMapper.createObjectNode();
        attributeSix.put("raw", placeOfBirth);

        ObjectNode attributeSeven = objectMapper.createObjectNode();
        attributeSeven.put("raw", image);

        ObjectNode attributeEight = objectMapper.createObjectNode();
        attributeEight.put("raw", sex);

        ObjectNode attributeNine = objectMapper.createObjectNode();
        attributeNine.put("raw", issuer);

        ObjectNode attributeTen = objectMapper.createObjectNode();
        attributeTen.put("raw", String.valueOf(someDate));

        ObjectNode attributeEleven = objectMapper.createObjectNode();
        attributeEleven.put("raw", "Passport");

        passportNode.set("number", attributeOne);
        passportNode.set("firstName", attributeTwo);
        passportNode.set("lastName", attributeThree);
        passportNode.set("fatherName", attributeFour);
        passportNode.set("dateOfBirth", attributeFive);
        passportNode.set("placeOfBirth", attributeSix);
        passportNode.set("image", attributeSeven);
        passportNode.set("sex", attributeEight);
        passportNode.set("issuer", attributeNine);
        passportNode.set("dateOfIssue", attributeTen);
        passportNode.set("resourceType", attributeEleven);
        return passportNode;
    }

    private static ObjectNode getMedicalImage(ObjectMapper objectMapper) {
        ObjectNode medicalImageNode = objectMapper.createObjectNode();

        ObjectNode attributeOne = objectMapper.createObjectNode();
        attributeOne.put("raw", "test");

        ObjectNode attributeTwo = objectMapper.createObjectNode();
        attributeTwo.put("raw", "MedicalImage");

        medicalImageNode.set("test", attributeOne);
        medicalImageNode.set("resourceType", attributeTwo);
        return medicalImageNode;
    }

    @BeforeEach
    void setUp() {
        credentialService = new CredentialServiceImpl(objectMapper, schemaService, identityService,
                walletService, userService, pool);
    }

    @ParameterizedTest
    @MethodSource("getCredentials_provideDocumentsAndResultExpectations")
    void getCredential_everyDocumentIsProvided_jsonStringIsReturned(Document document, String expected) {

        String credential = credentialService.getCredential(document);
        System.out.println("credential = " + credential);
        String result = credential.replaceAll(",\"encoded\":\"[^\"]*\"", "");

        assertEquals(expected, result);
    }

    @Test
    void getCredential_documentHasNullField_exceptionIsThrown() {
        NationalNumber nationalNumber = new NationalNumber(null, null, null);
        assertThrows(LedgerException.class, () -> credentialService.getCredential(nationalNumber));
    }

    @Test
    void getCredential_medicalImageHasNullMap_exceptionIsThrown() {
        MedicalImage medicalImage = new MedicalImage(null);
        assertThrows(LedgerException.class, () -> credentialService.getCredential(medicalImage));
    }

    @Test
    @SneakyThrows
    void getProofRequest_validInputs_proofRequestIdReturned() {
        Schema schema = Schema.builder().schemaDefinitionId("id").build();
        String number = "number";
        NationalNumber nationalNumber = new NationalNumber();
        nationalNumber.setNumber(number);

        ObjectNode expected = objectMapper.createObjectNode();
        expected.put("name", nationalNumber.getResourceType());
        expected.put("version", "1.0");

        ObjectNode restrictionsNode = objectMapper.createObjectNode();
        restrictionsNode.put("cred_def_id", schema.getSchemaDefinitionId());

        ObjectNode requestedAttributes = objectMapper.createObjectNode();

        ObjectNode typeAttribute = objectMapper.createObjectNode();
        typeAttribute.put("name", "resourceType");
        typeAttribute.set("restrictions", restrictionsNode);
        requestedAttributes.set("attr1_referent", typeAttribute);

        ObjectNode numberAttribute = objectMapper.createObjectNode();
        numberAttribute.put("name", "number");
        numberAttribute.set("restrictions", restrictionsNode);
        requestedAttributes.set("attr1_referent", numberAttribute);

        expected.set("requested_attributes", requestedAttributes);
        expected.set("requested_predicates", objectMapper.createObjectNode());

        String proofRequest = credentialService.getProofRequest(schema, nationalNumber);
        ObjectNode result = (ObjectNode) objectMapper.readTree(proofRequest);
        result.remove("nonce");

        assertEquals(expected, result);
    }

    @Test
    void getProofResponse_referentIsNotFound_exceptionHasBeenThrown() {
        assertThrows(ResourceNotFoundException.class, () -> credentialService.getProofResponse("{}", "{}"));
    }

    @Test
    @SneakyThrows
    void getProofResponse_validInputs_proofResponseStringIsReturned() {

        ObjectNode referent = objectMapper.createObjectNode();

        ObjectNode credInfo = objectMapper.createObjectNode();
        credInfo.set("referent", referent);
        ObjectNode element = objectMapper.createObjectNode();
        element.set("cred_info", credInfo);
        ArrayNode attrReferent = objectMapper.createArrayNode();
        attrReferent.add(element);
        ObjectNode attrs = objectMapper.createObjectNode();
        attrs.set("attr1_referent", attrReferent);
        ObjectNode suitableCredentials = objectMapper.createObjectNode();
        suitableCredentials.set("attrs", attrs);

        ObjectNode atributesNames = objectMapper.createObjectNode();
        atributesNames.put("test", "test");
        ObjectNode proofRequest = objectMapper.createObjectNode();
        proofRequest.set("requested_attributes", atributesNames);

        ObjectNode selfAttestedAttributes = objectMapper.createObjectNode();
        ObjectNode attribute = objectMapper.createObjectNode();
        attribute.put("cred_id", "{}");
        attribute.put("revealed", true);
        ObjectNode requestedAttributes = objectMapper.createObjectNode();
        requestedAttributes.set("test", attribute);
        ObjectNode requestedPredicates = objectMapper.createObjectNode();
        ObjectNode expected = objectMapper.createObjectNode();
        expected.set("self_attested_attributes", selfAttestedAttributes);
        expected.set("requested_attributes", requestedAttributes);
        expected.set("requested_predicates", requestedPredicates);

        String result = credentialService.getProofResponse(proofRequest.toString(), suitableCredentials.toString());

        assertEquals(expected, objectMapper.readTree(result));
    }

    @Test
    @SneakyThrows
    void addCredential_validInputs_methodIsExecuted() {

        String patientPublic = "patient public";
        String patientPrivate = "patient private";

        UserCredentials doctorCredentials = new UserCredentials("doctor public", "doctor private");
        Procedure document = new Procedure();
        document.setText(new Narrative(generated, "div"));
        document.setNotDoneReason(new CodeableConcept(null, "text"));
        document.setCode(new CodeableConcept(Collections.singletonList(new Coding()), "text"));
        document.setSubject(new Reference());
        document.setPerformedPeriod(new PerformedPeriod());
        document.setRecorder(new Reference());
        document.setAsserter(new Reference());
        document.setPerformer(new Reference());
        document.setReasonCode(Collections.singletonList(new CodeableConcept()));
        document.setBodySite(Collections.singletonList(new CodeableConcept()));
        document.setComplication(Collections.singletonList(new CodeableConcept()));
        document.setFollowUp(Collections.singletonList(new CodeableConcept()));
        document.setNote(Collections.singletonList(new CodeableConcept()));

        OfferRequest offerRequest = OfferRequest.builder()
                .doctorCredentials(doctorCredentials)
                .document(document)
                .build();

        Wallet patientWallet = mock(Wallet.class);
        Wallet doctorWallet = mock(Wallet.class);

        when(walletService.getOrCreateWallet(patientPublic, patientPrivate)).thenReturn(patientWallet);
        when(walletService.getOrCreateWallet(doctorCredentials.getPublicKey(), doctorCredentials.getPrivateKey()))
                .thenReturn(doctorWallet);

        Identity doctorIdentity = Identity.builder().build();
        when(identityService.findByWallet(doctorWallet)).thenReturn(doctorIdentity);

        credentialService = spy(credentialService);
        ArrayNode attributes = objectMapper.createArrayNode();
        doReturn(attributes).when(credentialService).prepareAttributes(any());

        Schema schema = Schema.builder().schema("").schemaId("")
                .schemaDefinition("def")
                .schemaDefinitionId("def id")
                .build();
        when(schemaService.getSchema(pool, doctorIdentity, document.getResourceType(),
                document.getResourceType().toLowerCase(), attributes)).thenReturn(schema);

        User doctor = User.builder().nationalNumber("number").build();
        when(userService.convert(doctorIdentity)).thenReturn(doctor);

        Identity patientIdentity = Identity.builder()
                .pseudonyms(Collections.singletonList(Pseudonym.builder()
                        .pseudonymDid("did")
                        .contact(Contact.builder()
                                .nationalNumber("number")
                                .build())
                        .build()))
                .build();
        when(identityService.findByWallet(patientWallet)).thenReturn(patientIdentity);

        doNothing().when(credentialService)
                .issueCredential(any(), any(), anyString(), anyString(), anyString(), any(), anyString());

        User patient = User.builder().build();
        when(userService.convert(patientIdentity)).thenReturn(patient);

        User result = credentialService.addCredential(patientPublic, patientPrivate, offerRequest);

        assertEquals(patient, result);
    }

    @Test
    @SneakyThrows
    void addCredential_exceptionOccursInsideTryBlock_exceptionIsThrown() {
        String patientPublic = "patient public";
        String patientPrivate = "patient private";

        UserCredentials doctorCredentials = new UserCredentials();
        Procedure document = new Procedure();

        OfferRequest offerRequest = OfferRequest.builder()
                .doctorCredentials(doctorCredentials)
                .document(document)
                .build();

        when(walletService.getOrCreateWallet(patientPublic, patientPrivate)).thenThrow(Exception.class);

        assertThrows(LedgerException.class, () -> credentialService.addCredential(patientPublic, patientPrivate, offerRequest));
    }

    @Test
    @SneakyThrows
    void addCredential_businessExceptionOccursInsideTryBlock_exceptionIsThrown() {
        String patientPublic = "patient public";
        String patientPrivate = "patient private";

        UserCredentials doctorCredentials = new UserCredentials();
        Procedure document = new Procedure();

        OfferRequest offerRequest = OfferRequest.builder()
                .doctorCredentials(doctorCredentials)
                .document(document)
                .build();

        when(walletService.getOrCreateWallet(patientPublic, patientPrivate)).thenThrow(ResourceNotFoundException.class);

        assertThrows(LedgerException.class, () -> credentialService.addCredential(patientPublic, patientPrivate, offerRequest));
    }

    @Test
    @SneakyThrows
    void issueCredential_validInputs_methodIsExecuted() {

        Wallet userWallet = mock(Wallet.class);
        Wallet trustAnchorWallet = mock(Wallet.class);
        String trustAnchorPseudonymDid = "gov did";

        String masterSecretId = "key";
        NationalNumber nationalNumber = new NationalNumber();
        String credential = "credential";

        AnoncredsResults.ProverCreateCredentialRequestResult credentialRequestResult = mock(AnoncredsResults.ProverCreateCredentialRequestResult.class);
        String credentialRequest = "cred req";
        String credentialRequestMetadata = "cred req metadata";
        when(credentialRequestResult.getCredentialRequestJson()).thenReturn(credentialRequest);
        when(credentialRequestResult.getCredentialRequestMetadataJson()).thenReturn(credentialRequestMetadata);

        AnoncredsResults.IssuerCreateCredentialResult issuerCreateCredentialResult = mock(AnoncredsResults.IssuerCreateCredentialResult.class);
        String credentialsResult = "creds result";
        String revocRegDelta = "delta";
        when(issuerCreateCredentialResult.getCredentialJson()).thenReturn(credentialsResult);
        when(issuerCreateCredentialResult.getRevocRegDeltaJson()).thenReturn(revocRegDelta);

        String schemaDefinitionId = "schema def id";
        String schemaDefinition = "schema def";

        String credentialOffer = "offer";

        new MockUp<Anoncreds>() {
            @mockit.Mock
            public CompletableFuture<String> issuerCreateCredentialOffer(Wallet wallet, String credDefId) {
                assertEquals(trustAnchorWallet, wallet);
                assertEquals(schemaDefinitionId, credDefId);

                CompletableFuture<String> future = new CompletableFuture<>();
                future.complete(credentialOffer);
                return future;
            }

            @mockit.Mock
            public CompletableFuture<AnoncredsResults.ProverCreateCredentialRequestResult> proverCreateCredentialReq(Wallet wallet,
                                                                                                                     String proverDid,
                                                                                                                     String credentialOfferJson,
                                                                                                                     String credentialDefJson,
                                                                                                                     String masterSecretId1) {
                assertEquals(userWallet, wallet);
                assertEquals(trustAnchorPseudonymDid, proverDid);
                assertEquals(credentialOffer, credentialOfferJson);
                assertEquals(schemaDefinition, credentialDefJson);
                assertEquals(masterSecretId, masterSecretId1);

                CompletableFuture<AnoncredsResults.ProverCreateCredentialRequestResult> future = new CompletableFuture<>();
                future.complete(credentialRequestResult);
                return future;
            }

            @mockit.Mock
            public CompletableFuture<AnoncredsResults.IssuerCreateCredentialResult> issuerCreateCredential(Wallet wallet,
                                                                                                           String credOfferJson,
                                                                                                           String credReqJson,
                                                                                                           String credValuesJson,
                                                                                                           String revRegId,
                                                                                                           int blobStorageReaderHandle) {
                assertEquals(trustAnchorWallet, wallet);
                assertEquals(credentialOffer, credOfferJson);
                assertEquals(credentialRequest, credReqJson);
                assertEquals(credential, credValuesJson);

                CompletableFuture<AnoncredsResults.IssuerCreateCredentialResult> future = new CompletableFuture<>();
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

        credentialService = spy(credentialService);
        doReturn(credential).when(credentialService).getCredential(nationalNumber);

        credentialService.issueCredential(userWallet, trustAnchorWallet, trustAnchorPseudonymDid, schemaDefinitionId,
                schemaDefinition, nationalNumber, masterSecretId);
    }

    @Test
    @SneakyThrows
    void deleteCredential_validInputs_methodIsExecuted() {
        String publicKey = "public";
        String privateKey = "private";
        String credentialId = "id";

        Wallet userWallet = mock(Wallet.class);

        when(walletService.getOrCreateWallet(publicKey, privateKey)).thenReturn(userWallet);

        new MockUp<Anoncreds>() {
            @mockit.Mock
            public CompletableFuture<Void> proverDeleteCredential(Wallet wallet, String credId) {
                assertEquals(userWallet, wallet);
                assertEquals(credentialId, credId);
                return CompletableFuture.runAsync(() -> log.debug("In mock."));
            }
        };

        credentialService.deleteCredential(publicKey, privateKey, credentialId);
    }

    @Test
    @SneakyThrows
    void deleteCredential_exceptionOccursInsideTryBlock_exceptionIsThrown() {
        String publicKey = "public";
        String privateKey = "private";
        String credentialId = "id";

        when(walletService.getOrCreateWallet(publicKey, privateKey)).thenThrow(ResourceNotFoundException.class);

        assertThrows(LedgerException.class, () -> credentialService.deleteCredential(publicKey, privateKey, credentialId));
    }

    @Test
    @SneakyThrows
    void prepareAttributes_medicalImageIsProvided_attributesAreReturned() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("test", "test");

        MedicalImage medicalImage = new MedicalImage(attributes);

        ArrayNode expected = objectMapper.createArrayNode();
        expected.add("test").add("resourceType");

        ArrayNode result = credentialService.prepareAttributes(medicalImage);

        assertEquals(expected, result);
    }

    @Test
    @SneakyThrows
    void prepareAttributes_nationalNumberIsProvided_attributesAreReturned() {
        NationalNumber nationalNumber = new NationalNumber();

        ArrayNode expected = objectMapper.createArrayNode();
        expected.add("number").add("registrationDate").add("issuer");

        ArrayNode result = credentialService.prepareAttributes(nationalNumber);

        assertEquals(expected, result);
    }


}
