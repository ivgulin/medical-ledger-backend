package com.mokujin.ssi.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mokujin.ssi.model.internal.Identity;
import com.mokujin.ssi.model.internal.Schema;
import com.mokujin.ssi.service.SchemaService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.indy.sdk.anoncreds.AnoncredsResults;
import org.hyperledger.indy.sdk.cache.Cache;
import org.hyperledger.indy.sdk.pool.Pool;
import org.springframework.stereotype.Service;

import static org.hyperledger.indy.sdk.anoncreds.Anoncreds.issuerCreateAndStoreCredentialDef;
import static org.hyperledger.indy.sdk.anoncreds.Anoncreds.issuerCreateSchema;
import static org.hyperledger.indy.sdk.ledger.Ledger.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchemaServiceImpl implements SchemaService {

    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    public Schema getSchema(Pool pool, Identity trustAnchor, String schemaName, String tag, ArrayNode attributes) {
        String version = "1.0";
        String schemaId = trustAnchor.getVerinymDid() + ":2:" + schemaName + ":" + version;
        String schemaDefinitionId;
        String schema;
        String schemaDefinition;
        try {
            schema = Cache.getSchema(pool, trustAnchor.getWallet(), trustAnchor.getVerinymDid(),
                    schemaId, "{}").get();
            log.info("'schema={}'", schema);

            JsonNode properties = objectMapper.readTree(schema);
            String seqNo = properties.get("seqNo").asText();

            schemaDefinitionId = trustAnchor.getVerinymDid() + ":3:CL:" + seqNo + ":" + tag;

            try {
                schemaDefinition = Cache.getCredDef(pool, trustAnchor.getWallet(),
                        trustAnchor.getVerinymDid(), schemaDefinitionId, "{}").get();
            } catch (Exception e) {
                log.error("Exception was thrown: " + e);
                schemaDefinition = this.createSchemaDefinition(pool, trustAnchor, tag, schema);
            }
        } catch (Exception e) {
            log.error("Exception was thrown: " + e);

            schema = this.createSchema(pool, trustAnchor, schemaName, version, attributes.toString());

            schemaDefinition = this.createSchemaDefinition(pool, trustAnchor, tag, schema);

            JsonNode properties = objectMapper.readTree(schemaDefinition);
            schemaDefinitionId = properties.get("id").asText();
        }

        return Schema.builder()
                .schemaId(schemaId)
                .schema(schema)
                .schemaDefinitionId(schemaDefinitionId)
                .schemaDefinition(schemaDefinition)
                .build();
    }

    @SneakyThrows
    String createSchema(Pool pool, Identity trustAnchor, String schemaName,
                        String version, String attributes) {
        log.info("'create schema attributes={}'", attributes);

        AnoncredsResults.IssuerCreateSchemaResult schemaBlueprint = issuerCreateSchema(
                trustAnchor.getVerinymDid(),
                schemaName,
                version,
                attributes).get();
        log.info("'schema={}'", schemaBlueprint);

        String schemaRequest = buildSchemaRequest(
                trustAnchor.getVerinymDid(),
                schemaBlueprint.getSchemaJson()).get();

        String schemaResponse = signAndSubmitRequest(
                pool,
                trustAnchor.getWallet(),
                trustAnchor.getVerinymDid(),
                schemaRequest).get();
        log.info("'schemaResponse={}'", schemaResponse);

        return Cache.getSchema(pool, trustAnchor.getWallet(), trustAnchor.getVerinymDid(),
                schemaBlueprint.getSchemaId(), "{}").get();
    }

    @SneakyThrows
    String createSchemaDefinition(Pool pool, Identity trustAnchor, String tag, String schema) {
        AnoncredsResults.IssuerCreateAndStoreCredentialDefResult schemaDefinition = issuerCreateAndStoreCredentialDef(
                trustAnchor.getWallet(),
                trustAnchor.getVerinymDid(),
                schema,
                tag,
                null,
                null)
                .get();

        String schemaDefinitionRequest = buildCredDefRequest(
                trustAnchor.getVerinymDid(),
                schemaDefinition.getCredDefJson()).get();
        log.info("'schemaDefinitionRequest={}'", schemaDefinitionRequest);
        String schemaDefinitionResponse = signAndSubmitRequest(
                pool,
                trustAnchor.getWallet(),
                trustAnchor.getVerinymDid(),
                schemaDefinitionRequest).get();
        log.info("'schemaDefinitionResponse={}'", schemaDefinitionResponse);

        return Cache.getCredDef(pool, trustAnchor.getWallet(),
                trustAnchor.getVerinymDid(), schemaDefinition.getCredDefId(), "{}").get();
    }
}
