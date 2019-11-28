package com.mokujin.ssi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mokujin.ssi.model.document.Document;
import com.mokujin.ssi.model.exception.extention.LedgerException;
import com.mokujin.ssi.model.government.document.NationalNumber;
import com.mokujin.ssi.model.government.document.NationalPassport;
import com.mokujin.ssi.service.CredentialService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// TODO: 11/26/2019 fix tests
class CredentialServiceImplTest {

    private CredentialService credentialService = new CredentialServiceImpl(new ObjectMapper(), null, null, null, null, null);

    private static Stream<Arguments> getCredentials_provideDocumentsAndResultExpectations() {

        String nationalNumber = "1234567890";
        long someDate = 1234567890L;
        String issuer = "government";
        String name = "John";
        String lastName = "Doe";
        String placeOfBirth = "place";
        String image = "encrypted";
        String sex = "male";
        String number = "number";

        ObjectMapper objectMapper = new ObjectMapper();

        ObjectNode passportNode = getPassportNode(someDate, issuer, name, lastName, placeOfBirth,
                image, sex, objectMapper, number);

        ObjectNode nationalNumberNode = getNationalNumber(nationalNumber, someDate, issuer, objectMapper);

        return Stream.of(
                Arguments.of(new NationalPassport(number, name, lastName, name, someDate, placeOfBirth,
                        image, sex, issuer, someDate), passportNode.toString()),
                Arguments.of(new NationalNumber(nationalNumber, someDate, issuer), nationalNumberNode.toString())
        );
    }

    private static ObjectNode getNationalNumber(String nationalNumber, long someDate, String issuer,
                                                ObjectMapper objectMapper) {
        ObjectNode nationalNumberNode = objectMapper.createObjectNode();

        ObjectNode attributeOne = objectMapper.createObjectNode();
        attributeOne.put("raw", nationalNumber);

        ObjectNode attributeTwo = objectMapper.createObjectNode();
        attributeTwo.put("raw", String.valueOf(someDate));

        ObjectNode attributeThree = objectMapper.createObjectNode();
        attributeThree.put("raw", issuer);

        ObjectNode attributeFour = objectMapper.createObjectNode();
        attributeFour.put("raw", "Number");

        nationalNumberNode.set("number", attributeOne);
        nationalNumberNode.set("registrationDate", attributeTwo);
        nationalNumberNode.set("issuer", attributeThree);
        nationalNumberNode.set("resourceType", attributeFour);
        return nationalNumberNode;
    }

    private static ObjectNode getPassportNode(long someDate, String issuer, String name, String lastName,
                                              String placeOfBirth, String image, String sex,
                                              ObjectMapper objectMapper, String number) {
        ObjectNode passportNode = objectMapper.createObjectNode();

        ObjectNode attributeOne = objectMapper.createObjectNode();
        attributeOne.put("raw", number);

        ObjectNode attributeTwo = objectMapper.createObjectNode();
        attributeTwo.put("raw", name);

        ObjectNode attributeThree = objectMapper.createObjectNode();
        attributeThree.put("raw", lastName);

        ObjectNode attributeFour = objectMapper.createObjectNode();
        attributeFour.put("raw", name);

        ObjectNode attributeFive = objectMapper.createObjectNode();
        attributeFive.put("raw", String.valueOf(someDate));

        ObjectNode attributeSix = objectMapper.createObjectNode();
        attributeSix.put("raw", placeOfBirth);

        ObjectNode attributeSeven = objectMapper.createObjectNode();
        attributeSeven.put("raw", image);

        ObjectNode attributeEight = objectMapper.createObjectNode();
        attributeEight.put("raw", sex);

        ObjectNode attributeNine = objectMapper.createObjectNode();
        attributeNine.put("raw", issuer);

        ObjectNode attributeTen = objectMapper.createObjectNode();
        attributeTen.put("raw", String.valueOf(someDate));

        ObjectNode attributeEleven = objectMapper.createObjectNode();
        attributeEleven.put("raw", "Passport");

        passportNode.set("number", attributeOne);
        passportNode.set("firstName", attributeTwo);
        passportNode.set("lastName", attributeThree);
        passportNode.set("fatherName", attributeFour);
        passportNode.set("dateOfBirth", attributeFive);
        passportNode.set("placeOfBirth", attributeSix);
        passportNode.set("image", attributeSeven);
        passportNode.set("sex", attributeEight);
        passportNode.set("issuer", attributeNine);
        passportNode.set("dateOfIssue", attributeTen);
        passportNode.set("resourceType", attributeEleven);
        return passportNode;
    }

    @ParameterizedTest
    @MethodSource("getCredentials_provideDocumentsAndResultExpectations")
    void getCredential_everyDocumentIsProvided_jsonStringIsReturned(Document document, String expected) {

        String credential = credentialService.getCredential(document);
        System.out.println("credential = " + credential);
        String result = credential.replaceAll(",\"encoded\":\"[^\"]*\"", "");

        assertEquals(expected, result);
    }

    @Test
    void getCredential_documentHasNullField_exceptionIsThrown() {
        NationalNumber nationalNumber = new NationalNumber(null, null, null);
        assertThrows(LedgerException.class, () -> credentialService.getCredential(nationalNumber));
    }
}