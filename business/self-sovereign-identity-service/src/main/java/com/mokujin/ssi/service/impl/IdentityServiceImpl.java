package com.mokujin.ssi.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mokujin.ssi.model.exception.extention.ResourceNotFoundException;
import com.mokujin.ssi.model.government.document.impl.NationalNumber;
import com.mokujin.ssi.model.government.document.impl.NationalPassport;
import com.mokujin.ssi.model.internal.*;
import com.mokujin.ssi.service.IdentityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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
    private final Pool pool;

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

        List<Credential> credentialList = credentials.equals("[]")
                ? new ArrayList<>()
                : objectMapper.readValue(credentials, new TypeReference<List<Credential>>() {
        });
        identity.setCredentials(credentialList);
        log.info("identity = '{}'", identity);

        return identity;
    }

    @Override
    public void establishUserConnection(Identity trustAnchor,
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

    @Override
    public void exchangeContacts(Identity doctorIdentity, Identity patientIdentity,
                                 CreateAndStoreMyDidResult patientPseudonym,
                                 CreateAndStoreMyDidResult doctorPseudonym) throws Exception {

        NationalPassport doctorPassport = getNationalPassport(doctorIdentity);
        NationalNumber doctorNationalNumber = getNationalNumber(doctorIdentity);
        this.addContact(patientIdentity, doctorPseudonym, doctorPassport, doctorNationalNumber);

        NationalPassport patientPassport = getNationalPassport(patientIdentity);
        NationalNumber patientNationalNumber = getNationalNumber(patientIdentity);
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
        System.out.println("contactJson = " + contactJson);
        Did.setDidMetadata(userIdentity.getWallet(), contactPseudonym.getDid(), contactJson).get();

        userIdentity.addPseudonym(Pseudonym.builder()
                .pseudonymDid(contactPseudonym.getDid())
                .contact(contact)
                .build());

    }

    private NationalNumber getNationalNumber(Identity identity) {
        return (NationalNumber) identity.getCredentials().stream()
                .filter(c -> NationalNumber.class.equals(c.getDocument().getClass()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No national number has been found."))
                .getDocument();
    }

    private NationalPassport getNationalPassport(Identity identity) {
        return (NationalPassport) identity.getCredentials().stream()
                .filter(c -> NationalPassport.class.equals(c.getDocument().getClass()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No passport has been found."))
                .getDocument();
    }
}
