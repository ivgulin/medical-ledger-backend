package com.mokujin.ssi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mokujin.ssi.model.government.document.Document;
import com.mokujin.ssi.model.government.document.impl.NationalNumber;
import com.mokujin.ssi.model.government.document.impl.NationalPassport;
import com.mokujin.ssi.service.CredentialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CredentialServiceImplTest {

    private CredentialService credentialService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        credentialService = new CredentialServiceImpl(objectMapper);
    }

    @ParameterizedTest
    @MethodSource("provideDocumentsAndResultExpectations")
    void getCredential_everyDocumentIsProvided_jsonStringIsReturned(Document document, String expected) {

        String credential = credentialService.getCredential(document);
        String result = credential.replaceAll(",\"encoded\":\"[^\"]*\"", "");

        assertEquals(expected, result);
    }

    private static Stream<Arguments> provideDocumentsAndResultExpectations() {

        String nationalNumber = "1234567890";
        long someDate = 1234567890L;
        String issuer = "government";
        String name = "John";
        String lastName = "Doe";
        String placeOfBirth = "place";
        String image = "encrypted";
        String sex = "male";

        ObjectMapper objectMapper = new ObjectMapper();

        ObjectNode passportNode = getPassportNode(someDate, issuer, name, lastName, placeOfBirth,
                image, sex, objectMapper);

        ObjectNode nationalNumberNode = getNationalNumber(nationalNumber, someDate, issuer, objectMapper);

        return Stream.of(
                Arguments.of(
                        NationalPassport.builder()
                                .firstName(name)
                                .lastName(lastName)
                                .fatherName(name)
                                .dateOfBirth(someDate)
                                .placeOfBirth(placeOfBirth)
                                .image(image)
                                .sex(sex)
                                .issuer(issuer)
                                .dateOfIssue(someDate)
                                .build()
                        ,
                        passportNode.toString()
                ),
                Arguments.of(
                        NationalNumber.builder()
                                .number(nationalNumber)
                                .registrationDate(someDate)
                                .issuer(issuer)
                                .build()
                        ,
                        nationalNumberNode.toString()
                )
        );
    }

    private static ObjectNode getNationalNumber(String nationalNumber, long someDate, String issuer,
                                                ObjectMapper objectMapper) {
        ObjectNode nationalNumberNode = objectMapper.createObjectNode();

        ObjectNode attributeOne = objectMapper.createObjectNode();
        attributeOne.put("raw", nationalNumber);

        ObjectNode attributeTwo = objectMapper.createObjectNode();
        attributeTwo.put("raw", someDate);

        ObjectNode attributeThree = objectMapper.createObjectNode();
        attributeThree.put("raw", issuer);

        nationalNumberNode.set("number", attributeOne);
        nationalNumberNode.set("registrationDate", attributeTwo);
        nationalNumberNode.set("issuer", attributeThree);
        return nationalNumberNode;
    }

    private static ObjectNode getPassportNode(long someDate, String issuer, String name, String lastName,
                                              String placeOfBirth, String image, String sex,
                                              ObjectMapper objectMapper) {
        ObjectNode passportNode = objectMapper.createObjectNode();

        ObjectNode attributeOne = objectMapper.createObjectNode();
        attributeOne.put("raw", name);

        ObjectNode attributeTwo = objectMapper.createObjectNode();
        attributeTwo.put("raw", lastName);

        ObjectNode attributeThree = objectMapper.createObjectNode();
        attributeThree.put("raw", name);

        ObjectNode attributeFour = objectMapper.createObjectNode();
        attributeFour.put("raw", someDate);

        ObjectNode attributeFive = objectMapper.createObjectNode();
        attributeFive.put("raw", placeOfBirth);

        ObjectNode attributeSix = objectMapper.createObjectNode();
        attributeSix.put("raw", image);

        ObjectNode attributeSeven = objectMapper.createObjectNode();
        attributeSeven.put("raw", sex);

        ObjectNode attributeEight = objectMapper.createObjectNode();
        attributeEight.put("raw", issuer);

        ObjectNode attributeNine = objectMapper.createObjectNode();
        attributeNine.put("raw", someDate);

        passportNode.set("firstName", attributeOne);
        passportNode.set("lastName", attributeTwo);
        passportNode.set("fatherName", attributeThree);
        passportNode.set("dateOfBirth", attributeFour);
        passportNode.set("placeOfBirth", attributeFive);
        passportNode.set("image", attributeSix);
        passportNode.set("sex", attributeSeven);
        passportNode.set("issuer", attributeEight);
        passportNode.set("dateOfIssue", attributeNine);
        return passportNode;
    }

}