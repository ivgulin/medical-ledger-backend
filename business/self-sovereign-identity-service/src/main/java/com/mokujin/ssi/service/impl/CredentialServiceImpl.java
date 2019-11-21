package com.mokujin.ssi.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mokujin.ssi.model.exception.extention.LedgerException;
import com.mokujin.ssi.model.exception.extention.ResourceNotFoundException;
import com.mokujin.ssi.model.government.document.Document;
import com.mokujin.ssi.model.government.document.NationalDocument;
import com.mokujin.ssi.model.internal.Schema;
import com.mokujin.ssi.service.CredentialService;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@Service
@AllArgsConstructor
public class CredentialServiceImpl implements CredentialService {

    private final ObjectMapper objectMapper;

    @Override
    public String getCredential(Document document) {
        List<Field> fields = Arrays.stream(document.getClass().getDeclaredFields()).collect(Collectors.toList());

        Class<?> superclass = document.getClass().getSuperclass() == NationalDocument.class
                ? document.getClass().getSuperclass().getSuperclass()
                : document.getClass().getSuperclass();

        fields.addAll(Arrays.stream(superclass.getDeclaredFields()).collect(Collectors.toList()));

        ObjectNode credentialNode = objectMapper.createObjectNode();

        fields.forEach(f -> {
            f.setAccessible(true);
            try {
                ObjectNode attribute = objectMapper.createObjectNode();
                Object value = f.get(document);
                attribute.put("raw", value.toString());
                attribute.put("encoded", String.valueOf(Math.abs(new Random().nextLong())));
                credentialNode.set(f.getName(), attribute);
            } catch (Exception e) {
                log.error("Exception was thrown: " + e);
                throw new LedgerException(INTERNAL_SERVER_ERROR, e.getMessage());
            }
        });

        return credentialNode.toString();
    }

    @Override
    public String getProofRequest(Schema schema, Document document) {

        ObjectNode proofRequestNode = objectMapper.createObjectNode();

        proofRequestNode.put("nonce", String.valueOf(Math.abs(new Random().nextLong())));
        proofRequestNode.put("name", StringUtils.capitalize(document.getType()));
        proofRequestNode.put("version", "1.0");

        ObjectNode restrictionsNode = objectMapper.createObjectNode();
        restrictionsNode.put("cred_def_id", schema.getSchemaDefinitionId());

        ObjectNode requestedAttributes = objectMapper.createObjectNode();
        proofRequestNode.set("requested_attributes", requestedAttributes);

        List<Field> fields = Arrays.stream(document.getClass().getDeclaredFields()).collect(Collectors.toList());

        int attributeNumber = 0;
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                ObjectNode attribute = objectMapper.createObjectNode();
                String fieldName = field.getName();
                Object value = field.get(document);
                if (Objects.nonNull(value)) {
                    attribute.put("name", fieldName);
                    attribute.set("restrictions", restrictionsNode);
                    requestedAttributes.set("attr" + ++attributeNumber + "_referent", attribute);
                }
            } catch (Exception e) {
                log.error("Exception was thrown: " + e);
                throw new LedgerException(INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }

        proofRequestNode.set("requested_predicates", objectMapper.createObjectNode());

        return proofRequestNode.toString();
    }

    @Override
    @SneakyThrows
    public String getProofResponse(String proofRequest, String suitableCredential) {

        ObjectNode proofResponseNode = objectMapper.createObjectNode();

        JsonNode credentialNode = objectMapper.readTree(suitableCredential);

        String referent = null;
        if (credentialNode.has("attrs")) {
            JsonNode tempNode = credentialNode.get("attrs");
            if (tempNode.has("attr1_referent")) {
                tempNode = tempNode.get("attr1_referent");
                if (tempNode.has(0)) {
                    tempNode = tempNode.get(0);
                    if (tempNode.has("cred_info")) {
                        tempNode = tempNode.get("cred_info");
                        if (tempNode.has("referent")) {
                            referent = tempNode.get("referent").toString().replace("\"", "");
                        }
                    }
                }
            }
        }
        if (Objects.isNull(referent)) {
            throw new ResourceNotFoundException("Credential has not been found.");
        }

        ObjectNode selfAttestedAttributes = objectMapper.createObjectNode();

        ObjectNode requestedAttributes = objectMapper.createObjectNode();
        JsonNode proofRequestNode = objectMapper.readTree(proofRequest);
        Iterator<String> attributesNames = proofRequestNode.get("requested_attributes").fieldNames();
        while (attributesNames.hasNext()) {
            ObjectNode attributeResponse = objectMapper.createObjectNode();
            attributeResponse.put("cred_id", referent);
            attributeResponse.put("revealed", true);
            requestedAttributes.set(attributesNames.next(), attributeResponse);

        }

        ObjectNode requestedPredicates = objectMapper.createObjectNode();

        proofResponseNode.set("self_attested_attributes", selfAttestedAttributes);
        proofResponseNode.set("requested_attributes", requestedAttributes);
        proofResponseNode.set("requested_predicates", requestedPredicates);

        return proofResponseNode.toString();

    }

    @Override
    @SneakyThrows
    public String getFormedCredential(String primaryCredential) {

        JsonNode credentialNode = objectMapper.readTree(primaryCredential);

        JsonNode formedCredential = null;
        if (credentialNode.has("attrs")) {
            JsonNode tempNode = credentialNode.get("attrs");
            if (tempNode.has("attr1_referent")) {
                tempNode = tempNode.get("attr1_referent");
                if (tempNode.has(0)) {
                    tempNode = tempNode.get(0);
                    if (tempNode.has("cred_info")) {
                        tempNode = tempNode.get("cred_info");
                        if (tempNode.has("attrs")) {
                            formedCredential = tempNode.get("attrs");
                        }
                    }
                }
            }
        }

        if (Objects.isNull(formedCredential)) {
            throw new ResourceNotFoundException("Credential wasn't found");
        }

        return formedCredential.toString();
    }
}
