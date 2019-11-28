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
    private String createSchema(Pool pool, Identity government, String schemaName,
                                String version, String attributes) {
        log.info("'create schema attributes={}'", attributes);

        AnoncredsResults.IssuerCreateSchemaResult schemaBlueprint = issuerCreateSchema(
                government.getVerinymDid(),
                schemaName,
                version,
                attributes).get();
        log.info("'schema={}'", schemaBlueprint);

        String schemaRequest = buildSchemaRequest(
                government.getVerinymDid(),
                schemaBlueprint.getSchemaJson()).get();

        String schemaResponse = signAndSubmitRequest(
                pool,
                government.getWallet(),
                government.getVerinymDid(),
                schemaRequest).get();
        log.info("'schemaResponse={}'", schemaResponse);

        return Cache.getSchema(pool, government.getWallet(), government.getVerinymDid(),
                schemaBlueprint.getSchemaId(), "{}").get();
    }

    @SneakyThrows
    private String createSchemaDefinition(Pool pool, Identity government, String tag, String schema) {
        AnoncredsResults.IssuerCreateAndStoreCredentialDefResult schemaDefinition = issuerCreateAndStoreCredentialDef(
                government.getWallet(),
                government.getVerinymDid(),
                schema,
                tag,
                null,
                null)
                .get();

        String schemaDefinitionRequest = buildCredDefRequest(
                government.getVerinymDid(),
                schemaDefinition.getCredDefJson()).get();
        log.info("'schemaDefinitionRequest={}'", schemaDefinitionRequest);
        String schemaDefinitionResponse = signAndSubmitRequest(
                pool,
                government.getWallet(),
                government.getVerinymDid(),
                schemaDefinitionRequest).get();
        log.info("'schemaDefinitionResponse={}'", schemaDefinitionResponse);

        return Cache.getCredDef(pool, government.getWallet(),
                government.getVerinymDid(), schemaDefinition.getCredDefId(), "{}").get();
    }
}
