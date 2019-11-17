package com.mokujin.ssi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mokujin.ssi.model.exception.BusinessException;
import com.mokujin.ssi.model.exception.extention.LedgerException;
import com.mokujin.ssi.model.government.KnownIdentity;
import com.mokujin.ssi.model.government.document.Document;
import com.mokujin.ssi.model.government.document.impl.Certificate;
import com.mokujin.ssi.model.government.document.impl.Diploma;
import com.mokujin.ssi.model.government.document.impl.NationalNumber;
import com.mokujin.ssi.model.government.document.impl.NationalPassport;
import com.mokujin.ssi.model.internal.Contact;
import com.mokujin.ssi.model.internal.Identity;
import com.mokujin.ssi.model.internal.Pseudonym;
import com.mokujin.ssi.model.internal.Schema;
import com.mokujin.ssi.model.user.request.UserRegistrationDetails;
import com.mokujin.ssi.model.user.response.User;
import com.mokujin.ssi.service.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.mokujin.ssi.model.government.KnownIdentity.Role.DOCTOR;
import static org.hyperledger.indy.sdk.anoncreds.Anoncreds.*;
import static org.hyperledger.indy.sdk.anoncreds.AnoncredsResults.IssuerCreateCredentialResult;
import static org.hyperledger.indy.sdk.anoncreds.AnoncredsResults.ProverCreateCredentialRequestResult;
import static org.hyperledger.indy.sdk.did.Did.createAndStoreMyDid;
import static org.hyperledger.indy.sdk.did.DidResults.CreateAndStoreMyDidResult;
import static org.hyperledger.indy.sdk.ledger.Ledger.buildNymRequest;
import static org.hyperledger.indy.sdk.ledger.Ledger.signAndSubmitRequest;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final ObjectMapper objectMapper;
    private final ValidationService validationService;
    private final WalletService walletService;
    private final IdentityService identityService;
    private final UserService userService;
    private final CredentialService credentialService;

    @Qualifier("government")
    private final Identity government;

    private final Pool pool;

    @Qualifier("passportSchema")
    private final Schema passportSchema;
    @Qualifier("nationalNumberSchema")
    private final Schema nationalNumberSchema;
    @Qualifier("certificateSchema")
    private final Schema certificateSchema;
    @Qualifier("diplomaSchema")

    private final Schema diplomaSchema;
    @Value(value = "${ledger.government.photo}")
    private String governmentPhoto;

    @Override
    public User register(UserRegistrationDetails details, String publicKey, String privateKey) {

        Wallet governmentWallet = government.getWallet();
        try (Wallet userWallet = walletService.getOrCreateWallet(publicKey, privateKey)) {

            Identity userIdentity = identityService.findByWallet(userWallet);

            if (userIdentity.getCredentials().isEmpty()) {

                KnownIdentity knownIdentity = validationService.validateNewbie(details);

                CreateAndStoreMyDidResult governmentPseudonym = createAndStoreMyDid(
                        governmentWallet,
                        "{}")
                        .get();
                CreateAndStoreMyDidResult userForGovernmentPseudonym = createAndStoreMyDid(
                        userIdentity.getWallet(),
                        "{}")
                        .get();
                identityService.establishUserConnection(government, governmentPseudonym, userForGovernmentPseudonym);

                if (knownIdentity.getRole().equals(DOCTOR)) {
                    this.grandVerinym(userIdentity, governmentWallet, knownIdentity);
                }

                this.exchangeContacts(userIdentity, knownIdentity, governmentWallet,
                        governmentPseudonym, userForGovernmentPseudonym);

                this.issueCredentials(publicKey, userWallet, governmentPseudonym, knownIdentity);

                userIdentity = identityService.findByWallet(userWallet);
            }

            User user = userService.convert(userIdentity);
            log.info("'user={}'", user);
            return user;

        } catch (Exception e) {
            log.error("Exception was thrown: " + e);
            if (e instanceof BusinessException) {
                throw new LedgerException(((BusinessException) e).getStatusCode(), e.getMessage());
            } else throw new LedgerException(INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void grandVerinym(Identity userIdentity, Wallet governmentWallet, KnownIdentity knownIdentity) throws Exception {
        CreateAndStoreMyDidResult verinym = createAndStoreMyDid(userIdentity.getWallet(), "{}").get();
        log.info("'verinym={}'", verinym);

        String firstName = knownIdentity.getNationalPassport().getFirstName();
        String lastName = knownIdentity.getNationalPassport().getLastName();
        String fatherName = knownIdentity.getNationalPassport().getFatherName();
        Contact selfContact = Contact.builder()
                .contactName(lastName + " " + firstName + " " + fatherName)
                .photo(knownIdentity.getNationalPassport().getImage())
                .nationalNumber(knownIdentity.getNationalNumber().getNumber())
                .isVisible(false)
                .build();
        String selfContactJson = objectMapper.writeValueAsString(selfContact);
        Did.setDidMetadata(userIdentity.getWallet(), verinym.getDid(), selfContactJson).get();

        userIdentity.setVerinymDid(verinym.getDid());

        String nymRegisterTrustAnchorVerinym = buildNymRequest(
                government.getVerinymDid(),
                userIdentity.getVerinymDid(),
                verinym.getVerkey(),
                null,
                "TRUST_ANCHOR").get();

        String nymRegisterTrustAnchorVerinymResponse = signAndSubmitRequest(
                pool,
                governmentWallet,
                government.getVerinymDid(),
                nymRegisterTrustAnchorVerinym).get();
        log.info("'nymRegisterDoctorVerinymResponse={}'", nymRegisterTrustAnchorVerinymResponse);
    }

    void exchangeContacts(Identity userIdentity, KnownIdentity knownIdentity, Wallet governmentWallet,
                          CreateAndStoreMyDidResult governmentPseudonym,
                          CreateAndStoreMyDidResult userForGovernmentPseudonym) throws Exception {
        Contact trustAnchorContactForUser = Contact.builder()
                .contactName("Government")
                .photo(governmentPhoto)
                .isVisible(false)
                .build();
        String trustAnchorContactForUserJson = objectMapper.writeValueAsString(trustAnchorContactForUser);
        System.out.println("trustAnchorContactForUserJson = " + trustAnchorContactForUserJson);
        Did.setDidMetadata(userIdentity.getWallet(), userForGovernmentPseudonym.getDid(), trustAnchorContactForUserJson).get();

        String firstName = knownIdentity.getNationalPassport().getFirstName();
        String lastName = knownIdentity.getNationalPassport().getLastName();
        String fatherName = knownIdentity.getNationalPassport().getFatherName();
        Contact userContactForTrustAnchor = Contact.builder()
                .contactName(lastName + " " + firstName + " " + fatherName)
                .photo(knownIdentity.getNationalPassport().getImage())
                .nationalNumber(knownIdentity.getNationalNumber().getNumber())
                .isVisible(false)
                .build();
        String userContactForTrustAnchorJson = objectMapper.writeValueAsString(userContactForTrustAnchor);
        System.out.println("userContactForTrustAnchorJson = " + userContactForTrustAnchorJson);
        Did.setDidMetadata(governmentWallet, governmentPseudonym.getDid(), userContactForTrustAnchorJson).get();

        userIdentity.addPseudonym(Pseudonym.builder()
                .pseudonymDid(userForGovernmentPseudonym.getDid())
                .contact(trustAnchorContactForUser)
                .build());

        government.addPseudonym(Pseudonym.builder()
                .pseudonymDid(governmentPseudonym.getDid())
                .contact(userContactForTrustAnchor)
                .build());
    }

    void issueCredentials(String publicKey, Wallet userWallet,
                          CreateAndStoreMyDidResult governmentPseudonym,
                          KnownIdentity knownIdentity) throws Exception {

        String masterSecretId = proverCreateMasterSecret(userWallet, publicKey).get();

        String nationalNumberSchemaDefinitionId = nationalNumberSchema.getSchemaDefinitionId();
        String nationalNumberSchemaDefinition = nationalNumberSchema.getSchemaDefinition();
        NationalNumber nationalNumber = knownIdentity.getNationalNumber();

        this.issueCredential(userWallet, governmentPseudonym, nationalNumberSchemaDefinitionId,
                nationalNumberSchemaDefinition, nationalNumber, masterSecretId);

        String passportSchemaDefinitionId = passportSchema.getSchemaDefinitionId();
        String passportSchemaDefinition = passportSchema.getSchemaDefinition();
        NationalPassport nationalPassport = knownIdentity.getNationalPassport();

        this.issueCredential(userWallet, governmentPseudonym, passportSchemaDefinitionId,
                passportSchemaDefinition, nationalPassport, masterSecretId);

        if (knownIdentity.getRole().equals(DOCTOR)) {
            String diplomaSchemaDefinitionId = diplomaSchema.getSchemaDefinitionId();
            String diplomaSchemaDefinition = diplomaSchema.getSchemaDefinition();
            Diploma diploma = knownIdentity.getDiploma();

            this.issueCredential(userWallet, governmentPseudonym, diplomaSchemaDefinitionId,
                    diplomaSchemaDefinition, diploma, masterSecretId);

            String certificateSchemaDefinitionId = certificateSchema.getSchemaDefinitionId();
            String certificationSchemaDefinition = certificateSchema.getSchemaDefinition();
            List<Certificate> certificates = knownIdentity.getCertificates();

            for (Certificate certificate : certificates) {
                this.issueCredential(userWallet, governmentPseudonym, certificateSchemaDefinitionId,
                        certificationSchemaDefinition, certificate, masterSecretId);
            }
        }

    }

    void issueCredential(Wallet userWallet, CreateAndStoreMyDidResult governmentPseudonym,
                         String schemaDefinitionId, String schemaDefinition,
                         Document document, String masterSecretId) throws Exception {
        String credentialOffer = issuerCreateCredentialOffer(
                government.getWallet(),
                schemaDefinitionId).get();
        log.info("'credentialOffer={}'", credentialOffer);

        ProverCreateCredentialRequestResult proverCreateCredentialRequestResult = proverCreateCredentialReq(
                userWallet,
                governmentPseudonym.getDid(),
                credentialOffer,
                schemaDefinition,
                masterSecretId)
                .get();
        log.info("'proverCreateCredentialRequestResult={}'", proverCreateCredentialRequestResult);

        String credential = credentialService.getCredential(document);
        log.info("'credential={}'", credential);

        IssuerCreateCredentialResult issuerCreateCredentialResult = issuerCreateCredential(
                government.getWallet(),
                credentialOffer,
                proverCreateCredentialRequestResult.getCredentialRequestJson(),
                credential,
                null,
                0)
                .get();
        log.info("'issuerCreateCredentialResult={}'", issuerCreateCredentialResult);

        String gottenCredential = proverStoreCredential(
                userWallet,
                null,
                proverCreateCredentialRequestResult.getCredentialRequestMetadataJson(),
                issuerCreateCredentialResult.getCredentialJson(),
                schemaDefinition,
                issuerCreateCredentialResult.getRevocRegDeltaJson()).get();
        log.info("'gottenCredential={}'", gottenCredential);
    }
}
