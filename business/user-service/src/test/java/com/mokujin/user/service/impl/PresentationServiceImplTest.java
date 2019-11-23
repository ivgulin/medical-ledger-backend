package com.mokujin.user.service.impl;

import com.mokujin.user.model.Contact;
import com.mokujin.user.model.User;
import com.mokujin.user.model.document.impl.NationalPassport;
import com.mokujin.user.model.notification.extention.PresentationNotification;
import com.mokujin.user.model.presentation.Affirmation;
import com.mokujin.user.model.presentation.PresentationAttributes;
import com.mokujin.user.model.presentation.PresentationRequest;
import com.mokujin.user.model.presentation.Proof;
import com.mokujin.user.service.NotificationService;
import com.mokujin.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.mokujin.user.model.User.Role;
import static com.mokujin.user.model.User.Role.DOCTOR;
import static com.mokujin.user.model.User.Role.PATIENT;
import static com.mokujin.user.model.document.Document.Type.*;
import static com.mokujin.user.model.notification.Notification.Type.PRESENTATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PresentationServiceImplTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserService userService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PresentationServiceImpl presentationService;

    private static Stream<Arguments> provideRolesAndResultExpectations() {

        List<String> passportAttributes = Arrays.asList("number", "firstName", "lastName",
                "fatherName", "dateOfBirth", "placeOfBirth", "image", "sex", "issuer", "dateOfIssue");
        List<String> nationalNumberAttributes = Arrays.asList("number", "registrationDate", "issuer");

        PresentationAttributes patientPresentationAttributes = new PresentationAttributes();
        patientPresentationAttributes.setPassportAttributes(passportAttributes);
        patientPresentationAttributes.setNationalNumberAttributes(nationalNumberAttributes);

        List<String> diplomaAttributes = Arrays.asList("number", "firstName", "lastName", "fatherName",
                "placeOfStudy", "courseOfStudy", "dateOfIssue", "qualification", "issuer");
        List<String> certificateAttributes = Arrays.asList("number", "firstName", "lastName", "fatherName",
                "dateOfExam", "dateOfIssue", "qualification", "courseOfStudy", "category", "expiresIn", "issuer");

        PresentationAttributes doctorPresentationAttributes = new PresentationAttributes();
        doctorPresentationAttributes.setPassportAttributes(passportAttributes);
        doctorPresentationAttributes.setNationalNumberAttributes(nationalNumberAttributes);
        doctorPresentationAttributes.setDiplomaAttributes(diplomaAttributes);
        doctorPresentationAttributes.setCertificateAttributes(certificateAttributes);

        return Stream.of(
                Arguments.of(PATIENT, patientPresentationAttributes),
                Arguments.of(DOCTOR, doctorPresentationAttributes)
        );
    }

    @ParameterizedTest
    @MethodSource("provideRolesAndResultExpectations")
    void getPresentationAttributes_validInputs_presentationAttributesAreReturned(Role role, PresentationAttributes expected) {

        PresentationAttributes result = presentationService.getPresentationAttributes(role);
        assertEquals(expected, result);
    }

    @Test
    void requestPresentation_allDocumentsAreRequested_methodIsExecuted() {

        String publicKey = "public";
        String privateKey = "private";

        List<String> passportAttributes = Arrays.asList("number", "firstName", "lastName",
                "fatherName", "dateOfBirth", "placeOfBirth", "image", "sex", "issuer", "dateOfIssue");
        List<String> nationalNumberAttributes = Arrays.asList("number", "registrationDate", "issuer");

        List<String> diplomaAttributes = Arrays.asList("number", "firstName", "lastName", "fatherName",
                "placeOfStudy", "courseOfStudy", "dateOfIssue", "qualification", "issuer");
        List<String> certificateAttributes = Arrays.asList("number", "firstName", "lastName", "fatherName",
                "dateOfExam", "dateOfIssue", "qualification", "courseOfStudy", "category", "expiresIn", "issuer");

        PresentationRequest presentationRequest = new PresentationRequest();
        presentationRequest.setPassportAttributes(passportAttributes);
        presentationRequest.setNationalNumberAttributes(nationalNumberAttributes);
        presentationRequest.setDiplomaAttributes(diplomaAttributes);
        presentationRequest.setCertificateAttributes(certificateAttributes);

        String connectionNumber = "number";

        User user = new User();
        when(userService.get(publicKey, privateKey)).thenReturn(user);

        when(notificationService.addPresentationNotification(user, passportAttributes, passport.name(), connectionNumber))
                .thenReturn(new PresentationNotification(null, PRESENTATION, Contact.builder().build(),
                        "", "", "", "", passport.name(), passportAttributes));
        when(notificationService.addPresentationNotification(user, nationalNumberAttributes, number.name(), connectionNumber))
                .thenReturn(new PresentationNotification(null, PRESENTATION, Contact.builder().build(),
                        "", "", "", "", number.name(), nationalNumberAttributes));
        when(notificationService.addPresentationNotification(user, diplomaAttributes, diploma.name(), connectionNumber))
                .thenReturn(new PresentationNotification(null, PRESENTATION, Contact.builder().build(),
                        "", "", "", "", diploma.name(), diplomaAttributes));
        when(notificationService.addPresentationNotification(user, certificateAttributes, certificate.name(), connectionNumber))
                .thenReturn(new PresentationNotification(null, PRESENTATION, Contact.builder().build(),
                        "", "", "", "", certificate.name(), certificateAttributes));

        presentationService.requestPresentation(publicKey, privateKey, presentationRequest, connectionNumber);

        verify(notificationService, times(4))
                .addPresentationNotification(any(), any(), anyString(), anyString());

    }

    @Test
    void presentProof_validInputs_methodIsExecuted() {
        String publicKey = "public";
        String privateKey = "private";

        NationalPassport passport = new NationalPassport();

        String connectionNumber = "number";

        Proof proof = new Proof();
        User user = new User();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<Proof> proofCaptor = ArgumentCaptor.forClass(Proof.class);
        ArgumentCaptor<String> numberCaptor = ArgumentCaptor.forClass(String.class);

        when(restTemplate.postForObject(anyString(), any(), any())).thenReturn(proof);
        when(userService.get(publicKey, privateKey)).thenReturn(user);
        when(notificationService.addProofNotification(userCaptor.capture(), proofCaptor.capture(), numberCaptor.capture()))
                .thenReturn(null);

        presentationService.presentProof(publicKey, privateKey, passport, connectionNumber);

        assertEquals(user, userCaptor.getValue());
        assertEquals(proof, proofCaptor.getValue());
        assertEquals(connectionNumber, numberCaptor.getValue());
    }

    @Test
    void verifyProof_validInputs_affirmationIsReturned() {

        String nationalNumber = "national number";
        String connectionNumber = "connection number";

        Proof proof = new Proof();

        Affirmation affirmation = Affirmation.builder().result(true).issuedBy(Contact.builder().build()).build();

        when(restTemplate.postForObject(anyString(), any(), any())).thenReturn(affirmation);

        Affirmation result = presentationService.verifyProof(proof, nationalNumber, connectionNumber);

        assertEquals(affirmation, result);
    }
}