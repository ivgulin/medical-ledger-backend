package com.mokujin.ssi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mokujin.ssi.model.exception.extention.LedgerException;
import com.mokujin.ssi.model.government.KnownIdentity;
import com.mokujin.ssi.model.government.document.NationalDocument;
import com.mokujin.ssi.model.internal.Contact;
import com.mokujin.ssi.model.internal.Identity;
import com.mokujin.ssi.model.internal.Schema;
import com.mokujin.ssi.model.user.request.UserRegistrationDetails;
import com.mokujin.ssi.model.verification.Verification;
import com.mokujin.ssi.model.verification.VerificationReport;
import com.mokujin.ssi.service.CredentialService;
import com.mokujin.ssi.service.ValidationService;
import com.mokujin.ssi.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static com.mokujin.ssi.model.government.document.Document.Type.*;
import static org.hyperledger.indy.sdk.anoncreds.Anoncreds.*;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;


@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationServiceImpl implements ValidationService {

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
    public KnownIdentity validateNewbie(UserRegistrationDetails details) {

        log.info("'validateNewbie' invoked with params'{}'", details);

        KnownIdentity knownIdentity = restTemplate
                .postForObject("http://government-service/identity/issue-credentials",
                        details, KnownIdentity.class);

        log.info("'validateNewbie' returned value '{}'", knownIdentity);

        return knownIdentity;
    }

    public Verification proveCompetence(String publicKey, String privateKey, NationalDocument nationalDocument) {

        try (Wallet wallet = walletService.getOrCreateWallet(publicKey, privateKey)) {

            ObjectNode schemaConfig = objectMapper.createObjectNode();
            ObjectNode credConfig = objectMapper.createObjectNode();

            String proofRequest;
            if (nationalDocument.getType().equals(passport.name())) {
                proofRequest = credentialService.getProofRequest(passportSchema, nationalDocument);
                schemaConfig.set(passportSchema.getSchemaId(), objectMapper.readTree(passportSchema.getSchema()));
                credConfig.set(passportSchema.getSchemaDefinitionId(),
                        objectMapper.readTree(passportSchema.getSchemaDefinition()));
            } else if (nationalDocument.getType().equals(number.name())) {
                proofRequest = credentialService.getProofRequest(nationalNumberSchema, nationalDocument);
                schemaConfig.set(nationalNumberSchema.getSchemaId(), objectMapper.readTree(nationalNumberSchema.getSchema()));
                credConfig.set(nationalNumberSchema.getSchemaDefinitionId(),
                        objectMapper.readTree(nationalNumberSchema.getSchemaDefinition()));
            } else if (nationalDocument.getType().equals(diploma.name())) {
                proofRequest = credentialService.getProofRequest(diplomaSchema, nationalDocument);
                schemaConfig.set(diplomaSchema.getSchemaId(), objectMapper.readTree(diplomaSchema.getSchema()));
                credConfig.set(diplomaSchema.getSchemaDefinitionId(),
                        objectMapper.readTree(diplomaSchema.getSchemaDefinition()));
            } else if (nationalDocument.getType().equals(certificate.name())) {
                proofRequest = credentialService.getProofRequest(certificateSchema, nationalDocument);
                schemaConfig.set(certificateSchema.getSchemaId(), objectMapper.readTree(certificateSchema.getSchema()));
                credConfig.set(certificateSchema.getSchemaDefinitionId(),
                        objectMapper.readTree(certificateSchema.getSchemaDefinition()));
            } else throw new LedgerException(NOT_FOUND, "No type of document was provided.");


            String suitableCredential = proverGetCredentialsForProofReq(
                    wallet,
                    proofRequest).get();
            log.info("'suitableCredential={}'", suitableCredential);

            String proofResponse = credentialService.getProofResponse(suitableCredential);
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

            return Verification.builder()
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

    public VerificationReport verifyProof(Verification verification) {
        try {
            Boolean isCryptographicallyVerified = verifierVerifyProof(
                    verification.getProofRequest(),
                    verification.getProofApplication(),
                    verification.getSchemaConfig(),
                    verification.getCredConfig(),
                    "{}",
                    "{}").get();
            log.info("'isCryptographicallyVerified={}'", isCryptographicallyVerified);

            // TODO: 21.11.2019 check dov did with output of proofApplication
            Contact issuedBy = Contact.builder()
                    .contactName("Government")
                    .photo(government.getImage())
                    .isVisible(true)
                    .build();

            return VerificationReport.builder()
                    .result(isCryptographicallyVerified)
                    .issuedBy(issuedBy)
                    .build();
        } catch (Exception e) {
            log.error("Exception was thrown: " + e);
            throw new LedgerException(INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
