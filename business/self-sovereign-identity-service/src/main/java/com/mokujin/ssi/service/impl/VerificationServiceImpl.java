package com.mokujin.ssi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mokujin.ssi.model.exception.extention.LedgerException;
import com.mokujin.ssi.model.government.KnownIdentity;
import com.mokujin.ssi.model.government.document.Document;
import com.mokujin.ssi.model.internal.Contact;
import com.mokujin.ssi.model.internal.Identity;
import com.mokujin.ssi.model.internal.Schema;
import com.mokujin.ssi.model.user.request.UserRegistrationDetails;
import com.mokujin.ssi.model.verification.Affirmation;
import com.mokujin.ssi.model.verification.Proof;
import com.mokujin.ssi.service.CredentialService;
import com.mokujin.ssi.service.VerificationService;
import com.mokujin.ssi.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static com.mokujin.ssi.model.government.document.Document.NationalDocumentType.Number;
import static com.mokujin.ssi.model.government.document.Document.NationalDocumentType.*;
import static org.hyperledger.indy.sdk.anoncreds.Anoncreds.*;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;


@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final RestTemplate restTemplate;
    private final CredentialService credentialService;
    private final WalletService walletService;
    private final ObjectMapper objectMapper;

    @Qualifier("government")
    private final Identity government;

    @Qualifier("passportSchema")
    private final Schema passportSchema;
    @Qualifier("nationalNumberSchema")
    private final Schema nationalNumberSchema;
    @Qualifier("certificateSchema")
    private final Schema certificateSchema;
    @Qualifier("diplomaSchema")
    private final Schema diplomaSchema;

    @Override
    public KnownIdentity verifyNewbie(UserRegistrationDetails details) {

        log.info("'verifyNewbie' invoked with params'{}'", details);

        KnownIdentity knownIdentity = restTemplate
                .postForObject("http://government-service/identity/issue-credentials",
                        details, KnownIdentity.class);

        log.info("'verifyNewbie' returned value '{}'", knownIdentity);

        return knownIdentity;
    }

    @Override
    public Proof presentProof(String publicKey, String privateKey, Document document) {

        try (Wallet wallet = walletService.getOrCreateWallet(publicKey, privateKey)) {

            ObjectNode schemaConfig = objectMapper.createObjectNode();
            ObjectNode credConfig = objectMapper.createObjectNode();

            String proofRequest;
            if (document.getResourceType().equals(Passport.name())) {
                proofRequest = credentialService.getProofRequest(passportSchema, document);
                schemaConfig.set(passportSchema.getSchemaId(), objectMapper.readTree(passportSchema.getSchema()));
                credConfig.set(passportSchema.getSchemaDefinitionId(),
                        objectMapper.readTree(passportSchema.getSchemaDefinition()));
            } else if (document.getResourceType().equals(Number.name())) {
                proofRequest = credentialService.getProofRequest(nationalNumberSchema, document);
                schemaConfig.set(nationalNumberSchema.getSchemaId(), objectMapper.readTree(nationalNumberSchema.getSchema()));
                credConfig.set(nationalNumberSchema.getSchemaDefinitionId(),
                        objectMapper.readTree(nationalNumberSchema.getSchemaDefinition()));
            } else if (document.getResourceType().equals(Diploma.name())) {
                proofRequest = credentialService.getProofRequest(diplomaSchema, document);
                schemaConfig.set(diplomaSchema.getSchemaId(), objectMapper.readTree(diplomaSchema.getSchema()));
                credConfig.set(diplomaSchema.getSchemaDefinitionId(),
                        objectMapper.readTree(diplomaSchema.getSchemaDefinition()));
            } else if (document.getResourceType().equals(Certificate.name())) {
                proofRequest = credentialService.getProofRequest(certificateSchema, document);
                schemaConfig.set(certificateSchema.getSchemaId(), objectMapper.readTree(certificateSchema.getSchema()));
                credConfig.set(certificateSchema.getSchemaDefinitionId(),
                        objectMapper.readTree(certificateSchema.getSchemaDefinition()));
            } else throw new LedgerException(NOT_FOUND, "No type of document was provided.");


            String suitableCredential = proverGetCredentialsForProofReq(
                    wallet,
                    proofRequest).get();
            log.info("'suitableCredential={}'", suitableCredential);

            String proofResponse = credentialService.getProofResponse(proofRequest, suitableCredential);
            log.info("'proofResponse={}'", proofResponse);

            String masterCardId = publicKey;

            String proofApplication = proverCreateProof(
                    wallet,
                    proofRequest,
                    proofResponse,
                    masterCardId,
                    schemaConfig.toString(),
                    credConfig.toString(),
                    "{}").get();
            log.info("'proofApplication={}'", proofApplication);

            String formedCredential = credentialService.getFormedCredential(suitableCredential);

            return Proof.builder()
                    .proofRequest(proofRequest)
                    .proofApplication(proofApplication)
                    .schemaConfig(schemaConfig.toString())
                    .credConfig(credConfig.toString())
                    .formedCredential(formedCredential)
                    .build();
        } catch (LedgerException e) {
            log.error("Exception was thrown: " + e);
            throw new LedgerException(e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Exception was thrown: " + e);
            throw new LedgerException(INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public Affirmation verifyProof(Proof proof) {
        try {
            Boolean isCryptographicallyVerified = verifierVerifyProof(
                    proof.getProofRequest(),
                    proof.getProofApplication(),
                    proof.getSchemaConfig(),
                    proof.getCredConfig(),
                    "{}",
                    "{}").get();
            log.info("'isCryptographicallyVerified={}'", isCryptographicallyVerified);

            // TODO: 21.11.2019 check dov did with output of proofApplication
            Contact issuedBy = Contact.builder()
                    .contactName("Government")
                    .photo(government.getImage())
                    .isVisible(true)
                    .build();

            return Affirmation.builder()
                    .result(isCryptographicallyVerified)
                    .issuedBy(issuedBy)
                    .build();
        } catch (Exception e) {
            log.error("Exception was thrown: " + e);
            throw new LedgerException(INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
