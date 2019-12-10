package com.mokujin.ssi.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mokujin.ssi.model.document.Document;
import com.mokujin.ssi.model.document.Document.MedicalDocumentType;
import com.mokujin.ssi.model.document.medical.dicom.MedicalImage;
import com.mokujin.ssi.model.document.medical.hl7.ModifiedProcedure;
import com.mokujin.ssi.model.document.medical.hl7.Procedure;
import com.mokujin.ssi.model.exception.BusinessException;
import com.mokujin.ssi.model.exception.extention.LedgerException;
import com.mokujin.ssi.model.exception.extention.ResourceNotFoundException;
import com.mokujin.ssi.model.internal.Identity;
import com.mokujin.ssi.model.internal.Schema;
import com.mokujin.ssi.model.user.request.OfferRequest;
import com.mokujin.ssi.model.user.request.UserCredentials;
import com.mokujin.ssi.model.user.response.User;
import com.mokujin.ssi.service.*;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.indy.sdk.anoncreds.AnoncredsResults;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static org.hyperledger.indy.sdk.anoncreds.Anoncreds.*;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@Service
@AllArgsConstructor
public class CredentialServiceImpl implements CredentialService {

    private final ObjectMapper objectMapper;
    private final SchemaService schemaService;
    private final IdentityService identityService;
    private final WalletService walletService;
    private final UserService userService;
    private final Pool pool;


    @Override
    public String getCredential(Document document) {
        ObjectNode credentialNode = objectMapper.createObjectNode();

        if (document.getResourceType().equals(MedicalDocumentType.MedicalImage.name())) {

            try {
                MedicalImage medicalImage = (MedicalImage) document;
                Map<String, String> attributes = medicalImage.getAttributes();
                attributes.put("resourceType", MedicalDocumentType.MedicalImage.name());

                attributes.forEach((k, v) -> {
                    ObjectNode attribute = objectMapper.createObjectNode();
                    attribute.put("raw", v);
                    attribute.put("encoded", String.valueOf(Math.abs(new Random().nextLong())));
                    credentialNode.set(k, attribute);

                });
            } catch (Exception e) {
                log.error("Exception was thrown: " + e);
                throw new LedgerException(INTERNAL_SERVER_ERROR, e.getMessage());
            }
        } else {
            List<Field> fields = Arrays.stream(document.getClass().getDeclaredFields()).collect(Collectors.toList());

            Class<?> superclass = document.getClass().getSuperclass();
            fields.addAll(Arrays.stream(superclass.getDeclaredFields()).collect(Collectors.toList()));

            Class<?> superiorClass = superclass.getSuperclass();
            fields.addAll(Arrays.stream(superiorClass.getDeclaredFields()).collect(Collectors.toList()));

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
                } finally {
                    f.setAccessible(false);
                }
            });
        }
        return credentialNode.toString();
    }

    @Override
    @SneakyThrows
    public String getProofRequest(Schema schema, Document document) {

        ObjectNode proofRequestNode = objectMapper.createObjectNode();

        proofRequestNode.put("nonce", String.valueOf(Math.abs(new Random().nextLong())));
        proofRequestNode.put("name", document.getResourceType());
        proofRequestNode.put("version", "1.0");

        ObjectNode restrictionsNode = objectMapper.createObjectNode();
        restrictionsNode.put("cred_def_id", schema.getSchemaDefinitionId());

        ObjectNode requestedAttributes = objectMapper.createObjectNode();
        proofRequestNode.set("requested_attributes", requestedAttributes);

        List<Field> fields = Arrays.stream(document.getClass().getDeclaredFields()).collect(Collectors.toList());

        int attributeNumber = 0;
        for (Field field : fields) {
            field.setAccessible(true);
            ObjectNode attribute = objectMapper.createObjectNode();
            String fieldName = field.getName();
            Object value = field.get(document);
            if (Objects.nonNull(value)) {
                attribute.put("name", fieldName);
                attribute.set("restrictions", restrictionsNode);
                requestedAttributes.set("attr" + ++attributeNumber + "_referent", attribute);
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
    public User addCredential(String publicKey, String privateKey, OfferRequest offerRequest) {

        UserCredentials credentials = offerRequest.getDoctorCredentials();
        Document document = offerRequest.getDocument();

        try (Wallet patientWallet = walletService.getOrCreateWallet(publicKey, privateKey);
             Wallet doctorWallet = walletService.getOrCreateWallet(credentials.getPublicKey(), credentials.getPrivateKey())) {

            Identity doctorIdentity = identityService.findByWallet(doctorWallet);
            String schemaName = document.getResourceType();
            String tag = schemaName.toLowerCase();

            if (document.getResourceType().equals(MedicalDocumentType.Procedure.name()))
                document = new ModifiedProcedure((Procedure) document);

            ArrayNode attributes = this.prepareAttributes(document);

            Schema schema = schemaService.getSchema(pool, doctorIdentity, schemaName, tag, attributes);
            log.info("'schema={}'", schema);

            User doctor = userService.convert(doctorIdentity);

            Identity patientIdentity = identityService.findByWallet(patientWallet);
            String doctorPseudonym = patientIdentity.getPseudonyms().stream()
                    .filter(pseudonym -> doctor.getNationalNumber().equals(pseudonym.getContact().getNationalNumber()))
                    .findFirst()
                    .get()
                    .getPseudonymDid();

            this.issueCredential(patientWallet, doctorWallet, doctorPseudonym, schema.getSchemaDefinitionId(),
                    schema.getSchemaDefinition(), document, publicKey);

            patientIdentity = identityService.findByWallet(patientWallet);
            return userService.convert(patientIdentity);
        } catch (BusinessException e) {
            log.error("Exception was thrown: " + e);
            throw new LedgerException(e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Exception was thrown: " + e);
            throw new LedgerException(INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public void issueCredential(Wallet userWallet, Wallet trustAnchorWallet, String trustAnchorPseudonym,
                                String schemaDefinitionId, String schemaDefinition, Document document,
                                String masterSecretId) throws Exception {

        String credentialOffer = issuerCreateCredentialOffer(
                trustAnchorWallet,
                schemaDefinitionId).get();
        log.info("'credentialOffer={}'", credentialOffer);

        AnoncredsResults.ProverCreateCredentialRequestResult proverCreateCredentialRequestResult = proverCreateCredentialReq(
                userWallet,
                trustAnchorPseudonym,
                credentialOffer,
                schemaDefinition,
                masterSecretId)
                .get();
        log.info("'proverCreateCredentialRequestResult={}'", proverCreateCredentialRequestResult);

        String credential = this.getCredential(document);
        log.info("'credential={}'", credential);

        AnoncredsResults.IssuerCreateCredentialResult issuerCreateCredentialResult = issuerCreateCredential(
                trustAnchorWallet,
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

    @Override
    public void deleteCredential(String publicKey, String privateKey, String credentialId) {
        try (Wallet wallet = walletService.getOrCreateWallet(publicKey, privateKey)) {
            proverDeleteCredential(wallet, credentialId);
        } catch (Exception e) {
            log.error("Exception was thrown: " + e);
            throw new LedgerException(INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    ArrayNode prepareAttributes(Document document) {
        ArrayNode response = objectMapper.createArrayNode();

        if (document.getResourceType().equals(MedicalDocumentType.MedicalImage.name())) {
            MedicalImage medicalImage = (MedicalImage) document;
            medicalImage.getAttributes().keySet().forEach(response::add);
            response.add("resourceType");
        } else {
            List<Field> fields = Arrays.stream(document.getClass().getDeclaredFields()).collect(Collectors.toList());
            Class<?> superclass = document.getClass().getSuperclass();
            fields.addAll(Arrays.stream(superclass.getDeclaredFields()).collect(Collectors.toList()));

            for (Field field : fields) {
                field.setAccessible(true);
                response.add(field.getName());
                field.setAccessible(false);
            }
        }
        return response;
    }
}