package com.mokujin.ssi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mokujin.ssi.model.internal.Contact;
import com.mokujin.ssi.model.internal.Identity;
import com.mokujin.ssi.model.internal.Pseudonym;
import com.mokujin.ssi.model.internal.Schema;
import com.mokujin.ssi.service.IdentityService;
import com.mokujin.ssi.service.WalletService;
import com.mokujin.ssi.service.SchemaService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.did.DidResults;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import static com.mokujin.ssi.model.internal.Role.STEWARD;
import static com.mokujin.ssi.model.internal.Role.TRUST_ANCHOR;
import static java.util.Objects.isNull;
import static org.hyperledger.indy.sdk.did.Did.createAndStoreMyDid;
import static org.hyperledger.indy.sdk.ledger.Ledger.*;
import static org.hyperledger.indy.sdk.pool.Pool.*;

@Slf4j
@Configuration
public class LedgerConfig {

    private final String TRANSACTIONS_GENESIS;

    private final Integer PROTOCOL_VERSION;

    private final String POOL_NAME;

    private final String STEWARD_SEED;

    private final String STEWARD_ID;

    private final String STEWARD_KEY;

    private final String GOVERNMENT_ID;

    private final String GOVERNMENT_KEY;

    private final String GOVERNMENT_PHOTO;

    private final WalletService walletService;

    private final IdentityService identityService;

    private final SchemaService schemaService;

    private final ObjectMapper objectMapper;

    @Autowired
    public LedgerConfig(@Value(value = "${ledger.genesis.path}") String transactionsGenesis,
                        @Value(value = "${ledger.pool.protocol.version}") Integer protocolVersion,
                        @Value(value = "${ledger.pool.name}") String poolName,
                        @Value(value = "${ledger.steward.seed}") String stewardSeed,
                        @Value(value = "${ledger.steward.wallet.id}") String stewardId,
                        @Value(value = "${ledger.steward.wallet.key}") String stewardKey,
                        @Value(value = "${ledger.government.wallet.id}") String governmentId,
                        @Value(value = "${ledger.government.wallet.key}") String governmentKey,
                        @Value(value = "${ledger.government.photo}") String governmentPhoto,
                        WalletService walletService, IdentityService identityService,
                        SchemaService schemaService, ObjectMapper objectMapper) {
        TRANSACTIONS_GENESIS = transactionsGenesis;
        PROTOCOL_VERSION = protocolVersion;
        POOL_NAME = poolName;
        STEWARD_SEED = stewardSeed;
        STEWARD_ID = stewardId;
        STEWARD_KEY = stewardKey;
        GOVERNMENT_ID = governmentId;
        GOVERNMENT_KEY = governmentKey;
        GOVERNMENT_PHOTO = governmentPhoto;
        this.walletService = walletService;
        this.identityService = identityService;
        this.schemaService = schemaService;
        this.objectMapper = objectMapper;
    }

    @Bean("pool")
    @SneakyThrows
    public Pool getPool() {
        setProtocolVersion(PROTOCOL_VERSION);

        ObjectNode poolConfig = objectMapper.createObjectNode();
        poolConfig.put("genesis_txn", TRANSACTIONS_GENESIS);
        try {
            createPoolLedgerConfig(POOL_NAME, poolConfig.toString()).get();
        } catch (Exception e) {
            log.error("Exception was thrown: " + e);
        }

        return openPoolLedger(POOL_NAME, "{}").get();
    }

    @Bean("stewardWallet")
    @DependsOn("pool")
    public Wallet getStewardWallet() {
        return this.getWallet(STEWARD_ID, STEWARD_KEY);
    }

    @Bean("governmentWallet")
    @DependsOn("pool")
    public Wallet getGovernmentWallet() {
        return this.getWallet(GOVERNMENT_ID, GOVERNMENT_KEY);
    }

    @SneakyThrows
    private Wallet getWallet(String id, String key) {
        return walletService.getOrCreateWallet(id, key);
    }

    @Bean("steward")
    @SneakyThrows
    @DependsOn("stewardWallet")
    public Identity getSteward(@Qualifier("stewardWallet") Wallet wallet) {
        ObjectNode seedConfig = objectMapper.createObjectNode();
        seedConfig.put("seed", STEWARD_SEED);

        Identity identity = identityService.findByWallet(wallet);
        identity.setRole(STEWARD);
        log.info("'stewardIdentity={}'", identity);

        if (isNull(identity.getVerinymDid())) {
            DidResults.CreateAndStoreMyDidResult verinym = createAndStoreMyDid(wallet, seedConfig.toString()).get();
            log.info("'verinym={}'", verinym);

            Contact selfContact = Contact.builder()
                    .contactName("Steward")
                    .isVerinym(true)
                    .isVisible(false)
                    .build();
            String selfContactJson = objectMapper.writeValueAsString(selfContact);
            Did.setDidMetadata(wallet, verinym.getDid(), selfContactJson).get();

            identity.setVerinymDid(verinym.getDid());
        }
        return identity;
    }

    @Bean("government")
    @SneakyThrows
    @DependsOn({"steward", "governmentWallet"})
    public Identity getGovernmentTrustAnchor(Pool pool, @Qualifier("governmentWallet") Wallet governmentWallet,
                                             @Qualifier("steward") Identity stewardIdentity) {
        return this.getIdentity(pool, stewardIdentity, governmentWallet, "Goverment", GOVERNMENT_PHOTO);
    }

    @SneakyThrows
    private Identity getIdentity(Pool pool, Identity stewardIdentity, Wallet trustAnchorWallet,
                                 String trustAnchorName, String trustAnchorPhoto) {

        Wallet stewardWallet = stewardIdentity.getWallet();

        Identity trustAnchorIdentity = identityService.findByWallet(trustAnchorWallet);
        trustAnchorIdentity.setRole(TRUST_ANCHOR);
        log.info("'trustAnchorIdentity={}'", trustAnchorIdentity);

        if (isNull(trustAnchorIdentity.getVerinymDid())) {
            DidResults.CreateAndStoreMyDidResult verinym = createAndStoreMyDid(trustAnchorWallet, "{}").get();
            log.info("'verinym={}'", verinym);

            Contact selfContact = Contact.builder()
                    .contactName(trustAnchorName)
                    .isVerinym(true)
                    .photo(trustAnchorPhoto)
                    .isVisible(false)
                    .build();
            String selfContactJson = objectMapper.writeValueAsString(selfContact);
            Did.setDidMetadata(trustAnchorWallet, verinym.getDid(), selfContactJson).get();

            trustAnchorIdentity.setVerinymDid(verinym.getDid());
            trustAnchorIdentity.setImage(trustAnchorPhoto);

            DidResults.CreateAndStoreMyDidResult stewardPseudonym = createAndStoreMyDid(
                    stewardWallet,
                    "{}")
                    .get();

            DidResults.CreateAndStoreMyDidResult trustAnchorPseudonym = createAndStoreMyDid(
                    trustAnchorWallet,
                    "{}")
                    .get();

            this.establishConnection(
                    pool,
                    stewardWallet,
                    verinym.getVerkey(),
                    stewardIdentity,
                    trustAnchorIdentity,
                    stewardPseudonym,
                    trustAnchorPseudonym);

            Contact trustAnchorContactForSteward = Contact.builder()
                    .contactName(trustAnchorName)
                    .photo(trustAnchorPhoto)
                    .isVisible(false)
                    .build();
            String trustAnchorContactForStewardJson = objectMapper.writeValueAsString(trustAnchorContactForSteward);
            Did.setDidMetadata(stewardWallet, stewardPseudonym.getDid(), trustAnchorContactForStewardJson).get();

            Contact stewardContactForTrustAnchor = Contact.builder()
                    .contactName("Steward")
                    .isVisible(false)
                    .build();
            String stewardContactForTrustAnchorJson = objectMapper.writeValueAsString(stewardContactForTrustAnchor);
            Did.setDidMetadata(trustAnchorWallet, trustAnchorPseudonym.getDid(), stewardContactForTrustAnchorJson).get();

            trustAnchorIdentity.addPseudonym(Pseudonym.builder()
                    .pseudonymDid(trustAnchorPseudonym.getDid())
                    .contact(stewardContactForTrustAnchor)
                    .build());

            stewardIdentity.addPseudonym(Pseudonym.builder()
                    .pseudonymDid(stewardPseudonym.getDid())
                    .contact(trustAnchorContactForSteward)
                    .build());

            log.info("'trustAnchorIdentity={}'", trustAnchorIdentity);
            log.info("'stewardIdentity={}'", stewardIdentity);
        }

        return trustAnchorIdentity;
    }

    @SneakyThrows
    private void establishConnection(Pool pool, Wallet stewardWallet, String trustAnchorVerKey,
                                     Identity stewardIdentity, Identity trustAnchorIdentity,
                                     DidResults.CreateAndStoreMyDidResult stewardPseudonym,
                                     DidResults.CreateAndStoreMyDidResult trustAnchorPseudonym) {

        String nymRegisterStewardPseudonym = buildNymRequest(
                stewardIdentity.getVerinymDid(),
                trustAnchorPseudonym.getDid(),
                trustAnchorPseudonym.getVerkey(),
                null,
                null).get();

        String nymRegisterStewardPseudonymResponse = signAndSubmitRequest(
                pool,
                stewardWallet,
                stewardIdentity.getVerinymDid(),
                nymRegisterStewardPseudonym).get();
        log.info("'nymRegisterStewardPseudonymResponse={}'", nymRegisterStewardPseudonymResponse);

        String nymRegisterTrustAnchorPseudonym = buildNymRequest(
                stewardIdentity.getVerinymDid(),
                stewardPseudonym.getDid(),
                stewardPseudonym.getVerkey(),
                null,
                null).get();

        String nymRegisterTrustAnchorPseudonymResponse = signAndSubmitRequest(
                pool,
                stewardWallet,
                stewardIdentity.getVerinymDid(),
                nymRegisterTrustAnchorPseudonym).get();
        log.info("'nymRegisterTrustAnchorPseudonymResponse={}'", nymRegisterTrustAnchorPseudonymResponse);

        String nymRegisterTrustAnchorVerinym = buildNymRequest(
                stewardIdentity.getVerinymDid(),
                trustAnchorIdentity.getVerinymDid(),
                trustAnchorVerKey,
                null,
                "ENDORSER").get();

        String nymRegisterTrustAnchorVerinymResponse = signAndSubmitRequest(
                pool,
                stewardWallet,
                stewardIdentity.getVerinymDid(),
                nymRegisterTrustAnchorVerinym).get();
        log.info("'nymRegisterTrustAnchorVerinymResponse={}'", nymRegisterTrustAnchorVerinymResponse);
    }


    @SneakyThrows
    @Bean("passportSchema")
    @DependsOn("government")
    public Schema getPassportSchema(Pool pool, @Qualifier("government") Identity government) {

        String schemaName = "Passport";
        String tag = "passport";

        ArrayNode attributes = objectMapper.createArrayNode();
        attributes
                .add("resourceType")
                .add("number")
                .add("firstName")
                .add("lastName")
                .add("fatherName")
                .add("dateOfBirth")
                .add("placeOfBirth")
                .add("image")
                .add("sex")
                .add("issuer")
                .add("dateOfIssue");

        Schema schema = schemaService.getSchema(pool, government, schemaName, tag, attributes);
        log.info("'passport schema={}'", schema);

        return schema;
    }

    @SneakyThrows
    @Bean("nationalNumberSchema")
    @DependsOn("government")
    public Schema getNationalNumberSchema(Pool pool, @Qualifier("government") Identity government) {

        String schemaName = "NationalNumber";
        String tag = "national_number";

        ArrayNode attributes = objectMapper.createArrayNode();
        attributes
                .add("resourceType")
                .add("number")
                .add("registrationDate")
                .add("issuer");

        Schema schema = schemaService.getSchema(pool, government, schemaName, tag, attributes);
        log.info("'national number schema={}'", schema);

        return schema;
    }

    @SneakyThrows
    @Bean("diplomaSchema")
    @DependsOn("government")
    public Schema getDiplomaSchema(Pool pool, @Qualifier("government") Identity government) {

        String schemaName = "Diploma";
        String tag = "diploma";

        ArrayNode attributes = objectMapper.createArrayNode();
        attributes
                .add("resourceType")
                .add("number")
                .add("firstName")
                .add("lastName")
                .add("fatherName")
                .add("placeOfStudy")
                .add("courseOfStudy")
                .add("dateOfIssue")
                .add("qualification")
                .add("issuer");

        Schema schema = schemaService.getSchema(pool, government, schemaName, tag, attributes);
        log.info("'diploma schema={}'", schema);

        return schema;
    }

    @SneakyThrows
    @Bean("certificateSchema")
    @DependsOn("government")
    public Schema getCertificateSchema(Pool pool, @Qualifier("government") Identity government) {

        String schemaName = "Certificate";
        String tag = "certificate";

        ArrayNode attributes = objectMapper.createArrayNode();
        attributes
                .add("resourceType")
                .add("number")
                .add("firstName")
                .add("lastName")
                .add("fatherName")
                .add("dateOfExam")
                .add("dateOfIssue")
                .add("qualification")
                .add("courseOfStudy")
                .add("category")
                .add("expiresIn")
                .add("issuer");

        Schema schema = schemaService.getSchema(pool, government, schemaName, tag, attributes);
        log.info("'certificate schema={}'", schema);

        return schema;
    }

}
