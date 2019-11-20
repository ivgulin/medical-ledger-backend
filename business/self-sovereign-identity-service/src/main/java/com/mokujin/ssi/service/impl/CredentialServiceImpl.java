package com.mokujin.ssi.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mokujin.ssi.model.exception.extention.LedgerException;
import com.mokujin.ssi.model.government.document.Document;
import com.mokujin.ssi.model.government.document.NationalDocument;
import com.mokujin.ssi.model.internal.Schema;
import com.mokujin.ssi.service.CredentialService;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
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

    // TODO: 21.11.2019 fix
    @Override
    public String getProofRequest(Schema schema, NationalDocument nationalDocument) {

        ObjectNode proofRequestNode = objectMapper.createObjectNode();

        proofRequestNode.put("nonce", String.valueOf(Math.abs(new Random().nextLong())));
        proofRequestNode.put("name", "Registration");
        proofRequestNode.put("version", "1.0");

        ObjectNode restrictionsNode = objectMapper.createObjectNode();
        restrictionsNode.put("cred_def_id", schema.getSchemaDefinitionId());

        ObjectNode attributeOne = objectMapper.createObjectNode();
        ObjectNode attributeTwo = objectMapper.createObjectNode();
        ObjectNode attributeThree = objectMapper.createObjectNode();
        ObjectNode attributeFour = objectMapper.createObjectNode();
        ObjectNode attributeFive = objectMapper.createObjectNode();
        ObjectNode attributeSix = objectMapper.createObjectNode();

        attributeOne.put("name", "first_name");
        attributeOne.set("restrictions", restrictionsNode);

        attributeTwo.put("name", "last_name");
        attributeTwo.set("restrictions", restrictionsNode);

        attributeThree.put("name", "day_of_birth");
        attributeThree.set("restrictions", restrictionsNode);

        attributeFour.put("name", "month_of_birth");
        attributeFour.set("restrictions", restrictionsNode);

        attributeFive.put("name", "year_of_birth");
        attributeFive.set("restrictions", restrictionsNode);

        attributeSix.put("name", "registry_number");
        attributeSix.set("restrictions", restrictionsNode);

        ObjectNode requestedAttributes = objectMapper.createObjectNode();

        proofRequestNode.set("requested_attributes", requestedAttributes);
        requestedAttributes.set("attr1_referent", attributeOne);
        requestedAttributes.set("attr2_referent", attributeTwo);
        requestedAttributes.set("attr3_referent", attributeThree);
        requestedAttributes.set("attr4_referent", attributeFour);
        requestedAttributes.set("attr5_referent", attributeFive);
        requestedAttributes.set("attr6_referent", attributeSix);

        proofRequestNode.set("requested_predicates", objectMapper.createObjectNode());

        return proofRequestNode.toString();
    }

    @Override
    @SneakyThrows
    public String getProofResponse(String suitableCredential) {

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
            throw new NoCredentialWasFoundException("Credential wasn't found");
        }

        ObjectNode selfAttestedAttributes = objectMapper.createObjectNode();
        ObjectNode requestedAttributes = objectMapper.createObjectNode();
        ObjectNode requestedPredicates = objectMapper.createObjectNode();

        ObjectNode attributeOne = objectMapper.createObjectNode();
        ObjectNode attributeTwo = objectMapper.createObjectNode();
        ObjectNode attributeThree = objectMapper.createObjectNode();
        ObjectNode attributeFour = objectMapper.createObjectNode();
        ObjectNode attributeFive = objectMapper.createObjectNode();
        ObjectNode attributeSix = objectMapper.createObjectNode();

        attributeOne.put("cred_id", referent);
        attributeOne.put("revealed", true);

        attributeTwo.put("cred_id", referent);
        attributeTwo.put("revealed", true);

        attributeThree.put("cred_id", referent);
        attributeThree.put("revealed", true);

        attributeFour.put("cred_id", referent);
        attributeFour.put("revealed", true);

        attributeFive.put("cred_id", referent);
        attributeFive.put("revealed", true);

        attributeSix.put("cred_id", referent);
        attributeSix.put("revealed", true);

        requestedAttributes.set("attr1_referent", attributeOne);
        requestedAttributes.set("attr2_referent", attributeTwo);
        requestedAttributes.set("attr3_referent", attributeThree);
        requestedAttributes.set("attr4_referent", attributeFour);
        requestedAttributes.set("attr5_referent", attributeFive);
        requestedAttributes.set("attr6_referent", attributeSix);

        proofResponseNode.set("self_attested_attributes", selfAttestedAttributes);
        proofResponseNode.set("requested_attributes", requestedAttributes);
        proofResponseNode.set("requested_predicates", requestedPredicates);

        return proofResponseNode.toString();

    }

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
            throw new NoCredentialWasFoundException("Credential wasn't found");
        }

        return formedCredential.toString();
    }
}
