package com.mokujin.ssi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mokujin.ssi.model.exception.LedgerException;
import com.mokujin.ssi.model.government.Document;
import com.mokujin.ssi.model.government.KnownIdentity;
import com.mokujin.ssi.model.government.NationalNumber;
import com.mokujin.ssi.model.government.NationalPassport;
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

import static java.util.Objects.isNull;
import static org.hyperledger.indy.sdk.anoncreds.Anoncreds.*;
import static org.hyperledger.indy.sdk.anoncreds.AnoncredsResults.IssuerCreateCredentialResult;
import static org.hyperledger.indy.sdk.anoncreds.AnoncredsResults.ProverCreateCredentialRequestResult;
import static org.hyperledger.indy.sdk.did.Did.createAndStoreMyDid;
import static org.hyperledger.indy.sdk.did.DidResults.CreateAndStoreMyDidResult;
import static org.hyperledger.indy.sdk.ledger.Ledger.buildNymRequest;
import static org.hyperledger.indy.sdk.ledger.Ledger.signAndSubmitRequest;

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

    @Value(value = "${ledger.government.photo}")
    private String governmentPhoto;

    @Qualifier("passportSchema")
    private final Schema passportSchema;

    @Qualifier("nationalNumberSchema")
    private final Schema nationalNumberSchema;

    @Override
    @SneakyThrows
    public User register(UserRegistrationDetails details, String publicKey, String privateKey) {

        ObjectNode config = objectMapper.createObjectNode();
        config.put("id", publicKey);
        ObjectNode credentials = objectMapper.createObjectNode();
        credentials.put("key", privateKey);
        Wallet userWallet = walletService.getOrCreateWallet(config.toString(), credentials.toString());

        Identity userIdentity = identityService.findByWallet(userWallet);

        User user;
        if (!isNull(userIdentity.getVerinymDid())) {
            return userService.convert(userIdentity);
        }

        KnownIdentity knownIdentity = validationService.validateNewbie(details);
        Wallet governmentWallet = government.getWallet();

        try {

            CreateAndStoreMyDidResult governmentPseudonym = createAndStoreMyDid(
                    governmentWallet,
                    "{}")
                    .get();
            CreateAndStoreMyDidResult userForGovernmentPseudonym = createAndStoreMyDid(
                    userIdentity.getWallet(),
                    "{}")
                    .get();
            this.establishUserConnection(government, governmentPseudonym, userForGovernmentPseudonym);

            this.exchangeContacts(userIdentity, knownIdentity, governmentWallet,
                    governmentPseudonym, userForGovernmentPseudonym);

            this.issueCredentials(publicKey, userWallet, governmentPseudonym, knownIdentity);

            userIdentity = identityService.findByWallet(userWallet);

            user = userService.convert(userIdentity);
            log.info("'user={}'", user);

        } catch (Exception e) {
            log.error("Exception was thrown: " + e);
            throw new LedgerException("Smth went wrong :(");
        } finally {
            userWallet.closeWallet().get();
        }
        return user;
    }

    @SneakyThrows
    private void exchangeContacts(Identity userIdentity, KnownIdentity knownIdentity, Wallet governmentWallet,
                                  CreateAndStoreMyDidResult governmentPseudonym,
                                  CreateAndStoreMyDidResult userForGovernmentPseudonym) {
        Contact trustAnchorContactForUser = Contact.builder()
                .contactName("Government")
                .photo(governmentPhoto)
                .build();
        String trustAnchorContactForUserJson = objectMapper.writeValueAsString(trustAnchorContactForUser);
        Did.setDidMetadata(userIdentity.getWallet(), userForGovernmentPseudonym.getDid(), trustAnchorContactForUserJson).get();

        String firstName = knownIdentity.getNationalPassport().getFirstName();
        String lastName = knownIdentity.getNationalPassport().getLastName();
        String fatherName = knownIdentity.getNationalPassport().getFatherName();
        Contact userContactForTrustAnchor = Contact.builder()
                .contactName(lastName + " " + firstName + " " + fatherName)
                .photo(knownIdentity.getNationalPassport().getImage())
                .build();
        String stewardContactForTrustAnchorJson = objectMapper.writeValueAsString(userContactForTrustAnchor);
        Did.setDidMetadata(governmentWallet, governmentPseudonym.getDid(), stewardContactForTrustAnchorJson).get();

        userIdentity.addPseudonym(Pseudonym.builder()
                .pseudonymDid(userForGovernmentPseudonym.getDid())
                .contact(trustAnchorContactForUser)
                .build());

        government.addPseudonym(Pseudonym.builder()
                .pseudonymDid(governmentPseudonym.getDid())
                .contact(userContactForTrustAnchor)
                .build());
    }

    @SneakyThrows
    private void establishUserConnection(Identity trustAnchor,
                                         CreateAndStoreMyDidResult trustAnchorPseudonym,
                                         CreateAndStoreMyDidResult userForTrustAnchorPseudonym) {

        String nymRegisterTrustAnchorPseudonym = buildNymRequest(
                trustAnchor.getVerinymDid(),
                userForTrustAnchorPseudonym.getDid(),
                userForTrustAnchorPseudonym.getVerkey(),
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

    private void issueCredentials(String publicKey, Wallet userWallet,
                                  CreateAndStoreMyDidResult governmentPseudonym,
                                  KnownIdentity knownIdentity) {

        String passportSchemaDefinitionId = passportSchema.getSchemaDefinitionId();
        String passportSchemaDefinition = passportSchema.getSchemaDefinition();
        NationalPassport nationalPassport = knownIdentity.getNationalPassport();

        issueCredential(publicKey, userWallet, governmentPseudonym, passportSchemaDefinitionId,
                passportSchemaDefinition, nationalPassport);

        String nationalNumberSchemaDefinitionId = nationalNumberSchema.getSchemaDefinitionId();
        String nationalNumberSchemaDefinition = nationalNumberSchema.getSchemaDefinition();
        NationalNumber nationalNumber = knownIdentity.getNationalNumber();

        issueCredential(publicKey, userWallet, governmentPseudonym, nationalNumberSchemaDefinitionId,
                nationalNumberSchemaDefinition, nationalNumber);

    }

    @SneakyThrows
    private void issueCredential(String publicKey, Wallet userWallet, CreateAndStoreMyDidResult governmentPseudonym,
                                 String schemaDefinitionId, String schemaDefinition, Document document) {
        String credentialOffer = issuerCreateCredentialOffer(
                government.getWallet(),
                schemaDefinitionId).get();
        log.info("'credentialOffer={}'", credentialOffer);

        String masterCardId = proverCreateMasterSecret(userWallet, publicKey).get();

        ProverCreateCredentialRequestResult proverCreateCredentialRequestResult = proverCreateCredentialReq(
                userWallet,
                governmentPseudonym.getDid(),
                credentialOffer,
                schemaDefinition,
                masterCardId)
                .get();
        log.info("'proverCreateCredentialRequestResult={}'", proverCreateCredentialRequestResult);

        String credential = credentialService.getCredential(document);

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
