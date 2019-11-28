package com.mokujin.ssi.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mokujin.ssi.model.document.Document.MedicalDocumentType;
import com.mokujin.ssi.model.document.medical.dicom.MedicalImage;
import com.mokujin.ssi.model.document.medical.hl7.LedgerModifiedProcedure;
import com.mokujin.ssi.model.document.medical.hl7.Procedure;
import com.mokujin.ssi.model.exception.extention.ResourceNotFoundException;
import com.mokujin.ssi.model.government.document.NationalNumber;
import com.mokujin.ssi.model.government.document.NationalPassport;
import com.mokujin.ssi.model.internal.*;
import com.mokujin.ssi.service.IdentityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mokujin.ssi.model.internal.Role.DOCTOR;
import static org.hyperledger.indy.sdk.anoncreds.Anoncreds.proverGetCredentials;
import static org.hyperledger.indy.sdk.did.DidResults.CreateAndStoreMyDidResult;
import static org.hyperledger.indy.sdk.ledger.Ledger.buildNymRequest;
import static org.hyperledger.indy.sdk.ledger.Ledger.signAndSubmitRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdentityServiceImpl implements IdentityService {

    private final ObjectMapper objectMapper;

    @Override
    public Identity findByWallet(Wallet wallet) throws Exception {

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
                .ifPresent(didWithMetadata -> {
                    identity.setVerinymDid(didWithMetadata.getDid());
                    identity.setRole(DOCTOR);
                    identity.setImage(didWithMetadata.getMetadata().getPhoto());
                });

        List<Pseudonym> pseudonyms = didsWithMetadata.stream()
                .filter(d -> !d.getMetadata().isVerinym())
                .map(d -> Pseudonym.builder()
                        .pseudonymDid(d.getDid())
                        .contact(d.getMetadata())
                        .build()).collect(Collectors.toList());
        identity.setPseudonyms(pseudonyms);

        String credentials = proverGetCredentials(wallet, "{}").get();
        log.info("'credentials={}'", credentials);

        List<Credential> credentialList = this.processCredentials(credentials);
        identity.setCredentials(credentialList);
        log.info("identity = '{}'", identity);

        return identity;
    }

    @Override
    public void establishUserConnection(Pool pool, Identity trustAnchor,
                                        CreateAndStoreMyDidResult trustAnchorPseudonym,
                                        CreateAndStoreMyDidResult userPseudonym) throws Exception {
        String nymRegisterTrustAnchorPseudonym = buildNymRequest(
                trustAnchor.getVerinymDid(),
                userPseudonym.getDid(),
                userPseudonym.getVerkey(),
                null,
                null).get();

        String nymRegisterTrustAnchorPseudonymResponse = signAndSubmitRequest(
                pool,
                trustAnchor.getWallet(),
                trustAnchor.getVerinymDid(),
                nymRegisterTrustAnchorPseudonym).get();
        log.info("'nymRegisterTrustAnchorPseudonymResponse={}'", nymRegisterTrustAnchorPseudonymResponse);

        String nymRegisterIdentityPseudonym = buildNymRequest(
                trustAnchor.getVerinymDid(),
                trustAnchorPseudonym.getDid(),
                trustAnchorPseudonym.getVerkey(),
                null,
                null).get();

        String nymRegisterIdentityPseudonymResponse = signAndSubmitRequest(
                pool,
                trustAnchor.getWallet(),
                trustAnchor.getVerinymDid(),
                nymRegisterIdentityPseudonym).get();
        log.info("'nymRegisterIdentityPseudonymResponse={}'", nymRegisterIdentityPseudonymResponse);
    }

    private List<Credential> processCredentials(String credentials) throws IOException {
        ArrayNode credentialsNode = (ArrayNode) objectMapper.readTree(credentials);

        MedicalImage medicalImage = null;
        Procedure procedure = null;
        for (JsonNode credentialNode : credentialsNode) {
            ObjectNode attrs = (ObjectNode) credentialNode.get("attrs");
            String resourceType = attrs.get("resourceType").textValue();

            if (resourceType.equals(MedicalDocumentType.MedicalImage.name())) {
                Map<String, String> dicomProperties = objectMapper
                        .convertValue(attrs, new TypeReference<HashMap<String, String>>() {
                        });
                medicalImage = new MedicalImage(dicomProperties);
                ((ObjectNode) credentialNode).remove("attrs");
            }
            if (resourceType.equals(MedicalDocumentType.Procedure.name())) {
                LedgerModifiedProcedure modifiedProcedure = objectMapper.convertValue(attrs, LedgerModifiedProcedure.class);
                procedure = new Procedure(modifiedProcedure);
                ((ObjectNode) credentialNode).remove("attrs");
            }
        }

        List<Credential> credentialList = credentials.equals("[]")
                ? new ArrayList<>()
                : objectMapper.convertValue(credentialsNode, new TypeReference<List<Credential>>() {
        });

        for (Credential credential : credentialList) {
            if (credential.getSchemaId().contains(MedicalDocumentType.MedicalImage.name()))
                credential.setDocument(medicalImage);
            if (credential.getSchemaId().contains(MedicalDocumentType.Procedure.name()))
                credential.setDocument(procedure);
        }
        return credentialList;
    }

    @Override
    public void exchangeContacts(Identity doctorIdentity, Identity patientIdentity,
                                 CreateAndStoreMyDidResult patientPseudonym,
                                 CreateAndStoreMyDidResult doctorPseudonym) throws Exception {

        NationalPassport doctorPassport = this.getNationalPassport(doctorIdentity);
        NationalNumber doctorNationalNumber = this.getNationalNumber(doctorIdentity);
        this.addContact(patientIdentity, doctorPseudonym, doctorPassport, doctorNationalNumber);

        NationalPassport patientPassport = this.getNationalPassport(patientIdentity);
        NationalNumber patientNationalNumber = this.getNationalNumber(patientIdentity);
        this.addContact(doctorIdentity, patientPseudonym, patientPassport, patientNationalNumber);
    }

    void addContact(Identity userIdentity, CreateAndStoreMyDidResult contactPseudonym,
                    NationalPassport contactPassport, NationalNumber contactNationalNumber) throws Exception {
        String firstName = contactPassport.getFirstName();
        String lastName = contactPassport.getLastName();
        String fatherName = contactPassport.getFatherName();
        Contact contact = Contact.builder()
                .contactName(lastName + " " + firstName + " " + fatherName)
                .photo(contactPassport.getImage())
                .nationalNumber(contactNationalNumber.getNumber())
                .isVisible(true)
                .build();
        String contactJson = objectMapper.writeValueAsString(contact);
        Did.setDidMetadata(userIdentity.getWallet(), contactPseudonym.getDid(), contactJson).get();

        userIdentity.addPseudonym(Pseudonym.builder()
                .pseudonymDid(contactPseudonym.getDid())
                .contact(contact)
                .build());

    }

    NationalNumber getNationalNumber(Identity identity) {
        return (NationalNumber) identity.getCredentials().stream()
                .filter(c -> NationalNumber.class.equals(c.getDocument().getClass()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No national number has been found."))
                .getDocument();
    }

    NationalPassport getNationalPassport(Identity identity) {
        return (NationalPassport) identity.getCredentials().stream()
                .filter(c -> NationalPassport.class.equals(c.getDocument().getClass()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No passport has been found."))
                .getDocument();
    }
}
