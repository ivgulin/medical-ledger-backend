package com.mokujin.ssi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mokujin.ssi.model.internal.Identity;
import com.mokujin.ssi.model.internal.Schema;
import lombok.SneakyThrows;
import mockit.Mock;
import mockit.MockUp;
import org.hyperledger.indy.sdk.anoncreds.Anoncreds;
import org.hyperledger.indy.sdk.cache.Cache;
import org.hyperledger.indy.sdk.ledger.Ledger;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

import static org.hyperledger.indy.sdk.anoncreds.AnoncredsResults.IssuerCreateAndStoreCredentialDefResult;
import static org.hyperledger.indy.sdk.anoncreds.AnoncredsResults.IssuerCreateSchemaResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchemaServiceImplTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    private SchemaServiceImpl schemaService = new SchemaServiceImpl(objectMapper);

    @Test
    @SneakyThrows
    void getSchema_noSchemaExists_schemaAndDefinitionAreCreatedAndReturned() {

        Pool pool = mock(Pool.class);
        Wallet trustAnchorWallet = mock(Wallet.class);
        String verinymDid = "did";
        Identity trustAnchor = Identity.builder()
                .wallet(trustAnchorWallet)
                .verinymDid(verinymDid)
                .build();

        String schemaName = "name";
        String tag = "tag";
        ArrayNode attributes = objectMapper.createArrayNode();

        String expectedSchemaId = "did:2:name:1.0";

        new MockUp<Cache>() {
            @Mock
            public CompletableFuture<String> getSchema(Pool pool, Wallet wallet, String submitterDid,
                                                       String id, String optionsJson) throws Exception {
                assertEquals(trustAnchorWallet, wallet);
                assertEquals(verinymDid, submitterDid);
                assertEquals(expectedSchemaId, id);

                throw new Exception("no schema exists");
            }
        };

        schemaService = spy(schemaService);

        String version = "1.0";
        String schema = "schema";
        doReturn(schema).when(schemaService).createSchema(pool, trustAnchor, schemaName, version, attributes.toString());

        String definitionId = "definition id";
        ObjectNode definition = objectMapper.createObjectNode();
        definition.put("id", definitionId);
        doReturn(definition.toString()).when(schemaService).createSchemaDefinition(pool, trustAnchor, tag, schema);

        Schema expected = Schema.builder()
                .schemaId(expectedSchemaId)
                .schema(schema)
                .schemaDefinitionId(definitionId)
                .schemaDefinition(definition.toString())
                .build();

        Schema result = schemaService.getSchema(pool, trustAnchor, schemaName, tag, attributes);

        assertEquals(expected, result);
    }

    @Test
    @SneakyThrows
    void getSchema_noDefinitionExists_definitionIsCreatedAndReturnedWithSchema() {

        Pool pool = mock(Pool.class);
        Wallet trustAnchorWallet = mock(Wallet.class);
        String verinymDid = "did";
        Identity trustAnchor = Identity.builder()
                .wallet(trustAnchorWallet)
                .verinymDid(verinymDid)
                .build();

        String schemaName = "name";
        String tag = "tag";
        ArrayNode attributes = objectMapper.createArrayNode();

        String expectedSchemaId = "did:2:name:1.0";
        String expectedDefinitionId = "did:3:CL:test:tag";

        ObjectNode schemaNode = objectMapper.createObjectNode();
        schemaNode.put("seqNo", "test");

        new MockUp<Cache>() {
            @Mock
            public CompletableFuture<String> getSchema(Pool pool, Wallet wallet, String submitterDid,
                                                       String id, String optionsJson) {
                assertEquals(trustAnchorWallet, wallet);
                assertEquals(verinymDid, submitterDid);
                assertEquals(expectedSchemaId, id);

                CompletableFuture<String> future = new CompletableFuture<>();
                future.complete(schemaNode.toString());
                return future;
            }

            @Mock
            public CompletableFuture<String> getCredDef(Pool pool, Wallet wallet, String submitterDid,
                                                        String id, String optionsJson) throws Exception {
                assertEquals(trustAnchorWallet, wallet);
                assertEquals(verinymDid, submitterDid);
                assertEquals(expectedDefinitionId, id);

                throw new Exception("no definition exists");
            }
        };

        schemaService = spy(schemaService);

        ObjectNode definition = objectMapper.createObjectNode();
        definition.put("id", expectedDefinitionId);
        doReturn(definition.toString()).when(schemaService)
                .createSchemaDefinition(pool, trustAnchor, tag, schemaNode.toString());

        Schema expected = Schema.builder()
                .schemaId(expectedSchemaId)
                .schema(schemaNode.toString())
                .schemaDefinitionId(expectedDefinitionId)
                .schemaDefinition(definition.toString())
                .build();

        Schema result = schemaService.getSchema(pool, trustAnchor, schemaName, tag, attributes);

        assertEquals(expected, result);
    }

    @Test
    void createSchema_validInputs_schemaIsReturned() {

        Pool pool = mock(Pool.class);
        Wallet trustAnchorWallet = mock(Wallet.class);
        String verinymDid = "did";
        Identity trustAnchor = Identity.builder()
                .wallet(trustAnchorWallet)
                .verinymDid(verinymDid)
                .build();

        String schemaName = "name";
        String schemaVersion = "1.0";
        ArrayNode attributes = objectMapper.createArrayNode();

        IssuerCreateSchemaResult createSchemaResult = mock(IssuerCreateSchemaResult.class);
        String schemaId = "schema id";
        String schemaJson = "schema json";
        when(createSchemaResult.getSchemaId()).thenReturn(schemaId);
        when(createSchemaResult.getSchemaJson()).thenReturn(schemaJson);
        new MockUp<Anoncreds>() {
            @Mock
            public CompletableFuture<IssuerCreateSchemaResult> issuerCreateSchema(String issuerDid, String name,
                                                                                  String version, String attrs) {
                assertEquals(verinymDid, issuerDid);
                assertEquals(schemaName, name);
                assertEquals(schemaVersion, version);
                assertEquals(attributes.toString(), attrs);

                CompletableFuture<IssuerCreateSchemaResult> future = new CompletableFuture<>();
                future.complete(createSchemaResult);
                return future;
            }
        };

        String schemaRequest = "schema request";
        new MockUp<Ledger>() {
            @Mock
            public CompletableFuture<String> buildSchemaRequest(String submitterDid, String data) {
                assertEquals(verinymDid, submitterDid);
                assertEquals(schemaJson, data);

                CompletableFuture<String> future = new CompletableFuture<>();
                future.complete(schemaRequest);
                return future;
            }

            @Mock
            public CompletableFuture<String> signAndSubmitRequest(Pool pool, Wallet wallet,
                                                                  String submitterDid, String requestJson) {
                assertEquals(trustAnchorWallet, wallet);
                assertEquals(verinymDid, submitterDid);
                assertEquals(schemaRequest, requestJson);

                CompletableFuture<String> future = new CompletableFuture<>();
                future.complete("");
                return future;
            }
        };

        new MockUp<Cache>() {
            @Mock
            public CompletableFuture<String> getSchema(Pool pool, Wallet wallet, String submitterDid,
                                                       String id, String optionsJson) {
                assertEquals(trustAnchorWallet, wallet);
                assertEquals(verinymDid, submitterDid);
                assertEquals(schemaId, id);

                CompletableFuture<String> future = new CompletableFuture<>();
                future.complete("");
                return future;
            }
        };

        schemaService.createSchema(pool, trustAnchor, schemaName, schemaVersion, attributes.toString());
    }

    @Test
    void createSchemaDefinition_validInputs_definitionIsReturned() {
        Pool pool = mock(Pool.class);
        Wallet trustAnchorWallet = mock(Wallet.class);
        String verinymDid = "did";
        Identity trustAnchor = Identity.builder()
                .wallet(trustAnchorWallet)
                .verinymDid(verinymDid)
                .build();

        String schema = "schema";
        String schemaTag = "tag";

        IssuerCreateAndStoreCredentialDefResult createDefResult = mock(IssuerCreateAndStoreCredentialDefResult.class);
        String defId = "def id";
        String defJson = "def json";
        when(createDefResult.getCredDefId()).thenReturn(defId);
        when(createDefResult.getCredDefJson()).thenReturn(defJson);
        new MockUp<Anoncreds>() {
            @Mock
            public CompletableFuture<IssuerCreateAndStoreCredentialDefResult> issuerCreateAndStoreCredentialDef(
                    Wallet wallet, String issuerDid, String schemaJson, String tag, String signatureType,
                    String configJson) {

                assertEquals(trustAnchorWallet, wallet);
                assertEquals(verinymDid, issuerDid);
                assertEquals(schema, schemaJson);
                assertEquals(schemaTag, tag);

                CompletableFuture<IssuerCreateAndStoreCredentialDefResult> future = new CompletableFuture<>();
                future.complete(createDefResult);
                return future;
            }
        };

        String defRequest = "def request";
        new MockUp<Ledger>() {
            @Mock
            public CompletableFuture<String> buildCredDefRequest(String submitterDid, String data) {
                assertEquals(verinymDid, submitterDid);
                assertEquals(defJson, data);

                CompletableFuture<String> future = new CompletableFuture<>();
                future.complete(defRequest);
                return future;
            }

            @Mock
            public CompletableFuture<String> signAndSubmitRequest(Pool pool, Wallet wallet,
                                                                  String submitterDid, String requestJson) {
                assertEquals(trustAnchorWallet, wallet);
                assertEquals(verinymDid, submitterDid);
                assertEquals(defRequest, requestJson);

                CompletableFuture<String> future = new CompletableFuture<>();
                future.complete("");
                return future;
            }
        };

        new MockUp<Cache>() {
            @Mock
            public CompletableFuture<String> getCredDef(Pool pool, Wallet wallet, String submitterDid,
                                                        String id, String optionsJson) throws Exception {
                assertEquals(trustAnchorWallet, wallet);
                assertEquals(verinymDid, submitterDid);
                assertEquals(defId, id);

                CompletableFuture<String> future = new CompletableFuture<>();
                future.complete("");
                return future;
            }
        };

        schemaService.createSchemaDefinition(pool, trustAnchor, schemaTag, schema);
    }

}