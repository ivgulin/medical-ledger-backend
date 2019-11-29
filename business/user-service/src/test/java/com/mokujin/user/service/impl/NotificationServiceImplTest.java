package com.mokujin.user.service.impl;

import com.mokujin.user.model.Contact;
import com.mokujin.user.model.ProcessedUserCredentials;
import com.mokujin.user.model.User;
import com.mokujin.user.model.chat.Message;
import com.mokujin.user.model.document.impl.medical.dicom.MedicalImage;
import com.mokujin.user.model.document.impl.medical.hl7.Procedure;
import com.mokujin.user.model.document.impl.national.NationalNumber;
import com.mokujin.user.model.notification.Notification;
import com.mokujin.user.model.notification.NotificationCollector;
import com.mokujin.user.model.notification.SystemNotification;
import com.mokujin.user.model.notification.extention.*;
import com.mokujin.user.model.presentation.Proof;
import com.mokujin.user.model.record.impl.BodyMeasurement;
import com.mokujin.user.model.record.impl.HeartHealthRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.mokujin.user.model.document.Document.NationalDocumentType.Number;
import static com.mokujin.user.model.document.Document.NationalDocumentType.Passport;
import static com.mokujin.user.model.notification.Notification.Type.*;
import static com.mokujin.user.model.notification.NotificationConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private RedissonClient redissonClient;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void getNotifications_validInputs_collectedNotificationsAreReturned() {

        String nationalNumber = "number";

        List<Message> messages = new ArrayList<>();
        messages.add(new Message("number", 123L, "hi", false));
        messages.add(new Message("number", 124L, "how r u", false));

        List<ChatNotification> messageNotifications = new ArrayList<>();
        messageNotifications.add(new ChatNotification(new Message("number", 123L, "hi", false)));
        messageNotifications.add(new ChatNotification(new Message("number", 124L, "how r u", false)));

        RList messagesList = mock(RList.class);
        when(redissonClient.getList("messages_" + nationalNumber)).thenReturn(messagesList);
        when(messagesList.stream()).thenReturn(messages.stream());


        List<SystemNotification> connectionNotifications = new ArrayList<>();
        connectionNotifications.add(new SystemNotification(123L, CONNECTION, Contact.builder().build(),
                "connection", "connection", "connection", "connection"));
        connectionNotifications.add(new SystemNotification(124L, CONNECTION, Contact.builder().build(),
                "connection", "connection", "connection", "connection"));

        RMap connectionsMap = mock(RMap.class);
        when(redissonClient.getMap("connections_" + nationalNumber)).thenReturn(connectionsMap);
        when(connectionsMap.values()).thenReturn(connectionNotifications);


        List<SystemNotification> invitationNotifications = new ArrayList<>();
        invitationNotifications.add(new SystemNotification(123L, INVITATION, Contact.builder().build(),
                "invitation", "invitation", "invitation", "invitation"));
        invitationNotifications.add(new SystemNotification(124L, INVITATION, Contact.builder().build(),
                "invitation", "invitation", "invitation", "invitation"));

        RMap invitationsMap = mock(RMap.class);
        when(redissonClient.getMap("invitations_" + nationalNumber)).thenReturn(invitationsMap);
        when(invitationsMap.values()).thenReturn(invitationNotifications);


        List<PresentationNotification> presentationNotifications = new ArrayList<>();
        presentationNotifications.add(new PresentationNotification(123L, Contact.builder().build(),
                "presentation", "presentation", "presentation", "presentation",
                Passport.name(), Collections.emptyList()));
        presentationNotifications.add(new PresentationNotification(124L, Contact.builder().build(),
                "presentation", "presentation", "presentation", "presentation",
                Number.name(), Collections.emptyList()));

        RMap presentationsMap = mock(RMap.class);
        when(redissonClient.getMap("presentations_" + nationalNumber)).thenReturn(presentationsMap);
        when(presentationsMap.values()).thenReturn(presentationNotifications);


        List<ProofNotification> proofNotifications = new ArrayList<>();
        proofNotifications.add(new ProofNotification(123L, Contact.builder().build(),
                "proof", "proof", "proof", "proof", new Proof()));
        proofNotifications.add(new ProofNotification(124L, Contact.builder().build(),
                "proof", "proof", "proof", "proof", new Proof()));

        RMap proofsMap = mock(RMap.class);
        when(redissonClient.getMap("proofs_" + nationalNumber)).thenReturn(proofsMap);
        when(proofsMap.values()).thenReturn(proofNotifications);


        List<HealthNotification> healthNotifications = new ArrayList<>();
        healthNotifications.add(new HealthNotification(123L, Contact.builder().build(),
                "health", "health", "health", "health", new HeartHealthRecord()));
        healthNotifications.add(new HealthNotification(124L, Contact.builder().build(),
                "health", "health", "health", "health", new BodyMeasurement()));

        RMap healthMap = mock(RMap.class);
        when(redissonClient.getMap("health_" + nationalNumber)).thenReturn(healthMap);
        when(healthMap.values()).thenReturn(healthNotifications);


        List<OfferNotification> offersNotifications = new ArrayList<>();
        offersNotifications.add(new OfferNotification(123L, Contact.builder().build(),
                "offer", "offer", "offer", "offer", new MedicalImage()));
        offersNotifications.add(new OfferNotification(124L, Contact.builder().build(),
                "offer", "offer", "offer", "offer", new Procedure()));

        RMap offersMap = mock(RMap.class);
        when(redissonClient.getMap("offers_" + nationalNumber)).thenReturn(offersMap);
        when(offersMap.values()).thenReturn(offersNotifications);


        List<AskNotification> askNotifications = new ArrayList<>();
        askNotifications.add(new AskNotification(123L, Contact.builder().build(),
                "ask", "ask", "ask", "ask", Collections.emptyList()));
        askNotifications.add(new AskNotification(124L, Contact.builder().build(),
                "ask", "ask", "ask", "ask", Collections.emptyList()));

        RMap asksMap = mock(RMap.class);
        when(redissonClient.getMap("asks_" + nationalNumber)).thenReturn(asksMap);
        when(asksMap.values()).thenReturn(askNotifications);


        List<DocumentNotification> documentNotifications = new ArrayList<>();
        documentNotifications.add(new DocumentNotification(123L, Contact.builder().build(),
                "document", "document", "document", "document", new MedicalImage()));
        documentNotifications.add(new DocumentNotification(124L, Contact.builder().build(),
                "document", "document", "document", "document", new Procedure()));

        RMap documentsMap = mock(RMap.class);
        when(redissonClient.getMap("documents_" + nationalNumber)).thenReturn(documentsMap);
        when(documentsMap.values()).thenReturn(documentNotifications);

        NotificationCollector expected = NotificationCollector.builder()
                .messages(messageNotifications)
                .connections(connectionNotifications)
                .invitations(invitationNotifications)
                .presentations(presentationNotifications)
                .proofs(proofNotifications)
                .health(healthNotifications)
                .offers(offersNotifications)
                .asks(askNotifications)
                .documents(documentNotifications)
                .build();

        NotificationCollector result = notificationService.getNotifications(nationalNumber);

        assertEquals(expected, result);
    }

    @Test
    void addInviteNotification_validInputs_notificationIsReturned() {

        Contact doctor = Contact.builder()
                .contactName("doctor")
                .nationalNumber("doctor's number")
                .build();

        User patient = new User();
        patient.setFirstName("John");
        patient.setLastName("Doe");
        patient.setFirstName("John");
        patient.setPhoto("photo");
        patient.setNationalNumber("patient's number");

        String patientPublicKey = "public";
        String patientPrivateKey = "private";

        ArgumentCaptor<String> credentialKey = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ProcessedUserCredentials> credentialValue = ArgumentCaptor.forClass(ProcessedUserCredentials.class);

        ArgumentCaptor<String> connectionsKey = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<SystemNotification> connectionsValue = ArgumentCaptor.forClass(SystemNotification.class);

        ArgumentCaptor<String> invitationsKey = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<SystemNotification> invitationsValue = ArgumentCaptor.forClass(SystemNotification.class);

        RMap credentials = mock(RMap.class);
        when(redissonClient.getMap("credentials")).thenReturn(credentials);
        when(credentials.put(credentialKey.capture(), credentialValue.capture())).thenReturn(null);

        RMap doctorNotifications = mock(RMap.class);
        when(redissonClient.getMap("connections_" + doctor.getNationalNumber())).thenReturn(doctorNotifications);
        when(doctorNotifications.put(connectionsKey.capture(), connectionsValue.capture())).thenReturn(null);

        RMap patientNotifications = mock(RMap.class);
        when(redissonClient.getMap("invitations_" + patient.getNationalNumber())).thenReturn(patientNotifications);
        when(patientNotifications.put(invitationsKey.capture(), invitationsValue.capture())).thenReturn(null);

        Notification result = notificationService
                .addInviteNotification(patientPublicKey, patientPrivateKey, doctor, patient);
        result.setDate(null);

        Notification expected = new SystemNotification(null, INVITATION, doctor, "", "",
                INVITATION_CONTENT_EN, INVITATION_CONTENT_UKR);


        assertEquals(expected, result);

        assertEquals(doctor.getNationalNumber() + patient.getNationalNumber(), credentialKey.getValue());

        assertEquals(ProcessedUserCredentials.builder()
                .publicKey(patientPublicKey)
                .privateKey(patientPrivateKey)
                .build(), credentialValue.getValue());

        assertEquals(patientPublicKey, connectionsKey.getValue());

        SystemNotification connectionValue = connectionsValue.getValue();
        connectionValue.setDate(null);
        assertEquals(new SystemNotification(null, CONNECTION,
                Contact.builder()
                        .contactName(patient.getLastName() + " " + patient.getFirstName() + " " + patient.getFatherName())
                        .photo(patient.getPhoto())
                        .nationalNumber(patient.getNationalNumber())
                        .isVisible(true)
                        .build(), "", "", CONNECTION_CONTENT_EN, CONNECTION_CONTENT_UKR), connectionValue);

        assertEquals(doctor.getNationalNumber(), invitationsKey.getValue());

        SystemNotification invitationValue = invitationsValue.getValue();
        invitationValue.setDate(null);
        assertEquals(expected, invitationValue);
    }

    @Test
    void removeInviteNotification_validInputs_credentialsAreReturned() {

        String doctorNumber = "doctor's number";
        String patientNumber = "patient's number";

        ProcessedUserCredentials userCredentials = ProcessedUserCredentials.builder()
                .publicKey("public")
                .privateKey("private")
                .build();

        ArgumentCaptor<String> getCredentialKey = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> removeCredentialKey = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> connectionsKey = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> invitationsKey = ArgumentCaptor.forClass(String.class);

        RMap credentials = mock(RMap.class);
        when(redissonClient.getMap("credentials")).thenReturn(credentials);
        when(credentials.get(getCredentialKey.capture())).thenReturn(userCredentials);
        when(credentials.remove(removeCredentialKey.capture())).thenReturn(null);

        RMap doctorNotifications = mock(RMap.class);
        when(redissonClient.getMap("connections_" + doctorNumber)).thenReturn(doctorNotifications);
        when(doctorNotifications.remove(connectionsKey.capture())).thenReturn(null);

        RMap patientNotifications = mock(RMap.class);
        when(redissonClient.getMap("invitations_" + patientNumber)).thenReturn(patientNotifications);
        when(patientNotifications.remove(invitationsKey.capture())).thenReturn(null);

        ProcessedUserCredentials result = notificationService.removeInviteNotification(doctorNumber, patientNumber);

        assertEquals(userCredentials, result);

        assertEquals(doctorNumber + patientNumber, getCredentialKey.getValue());

        assertEquals(doctorNumber + patientNumber, removeCredentialKey.getValue());

        assertEquals(userCredentials.getPublicKey(), connectionsKey.getValue());

        assertEquals(doctorNumber, invitationsKey.getValue());
    }

    @Test
    void addMessage_validInputs_notificationIsReturned() {
        String number = "number";
        Message message = new Message("number", 123L, "message", false);

        RList messagesList = mock(RList.class);
        when(redissonClient.getList("messages_" + number)).thenReturn(messagesList);
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        when(messagesList.add(messageCaptor.capture())).thenReturn(true);

        Notification result = notificationService.addMessage(number, message);
        ChatNotification expected = new ChatNotification(message);

        assertEquals(expected, result);
        assertEquals(message, messageCaptor.getValue());
    }

    @Test
    void removeMessage_validInputs_notificationIsDeleted() {
        String number = "number";
        Message message = new Message("number", 123L, "message", false);

        RList messagesList = mock(RList.class);
        when(redissonClient.getList("messages_" + number)).thenReturn(messagesList);
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        when(messagesList.remove(messageCaptor.capture())).thenReturn(true);

        notificationService.removeMessage(number, message);

        assertEquals(message, messageCaptor.getValue());
    }

    @Test
    void addPresentationNotification_validInputs_notificationIsReturned() {
        String number = "number";
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setFirstName("John");
        user.setPhoto("photo");
        user.setNationalNumber("another number");

        ArgumentCaptor<String> presentationKey = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<PresentationNotification> presentationValue = ArgumentCaptor.forClass(PresentationNotification.class);

        RMap presentationNotifications = mock(RMap.class);
        when(redissonClient.getMap("presentations_" + number)).thenReturn(presentationNotifications);
        when(presentationNotifications.put(presentationKey.capture(), presentationValue.capture())).thenReturn(null);

        ArrayList<String> presentationAttributes = new ArrayList<>();
        presentationAttributes.add("firstName");
        presentationAttributes.add("lastName");
        Notification result = notificationService.addPresentationNotification(user,
                presentationAttributes, Passport.name(), number);

        assertEquals(user.getNationalNumber() + Passport.name(), presentationKey.getValue());
        assertEquals(result, presentationValue.getValue());
    }


    @Test
    void removePresentationNotification_validInputs_notificationIsDeleted() {
        String number = "number";
        User user = new User();
        user.setNationalNumber("another number");

        ArgumentCaptor<String> connectionNumberCaptor = ArgumentCaptor.forClass(String.class);

        RMap presentationNotifications = mock(RMap.class);
        when(redissonClient.getMap("presentations_" + user.getNationalNumber())).thenReturn(presentationNotifications);
        when(presentationNotifications.remove(connectionNumberCaptor.capture())).thenReturn(null);

        notificationService.removePresentationNotification(user.getNationalNumber(), number, number);

        assertEquals(number + number, connectionNumberCaptor.getValue());
    }

    @Test
    void addProofNotification_validInputs_notificationIsReturned() {
        String number = "number";
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setFirstName("John");
        user.setPhoto("photo");
        user.setNationalNumber("another number");
        Proof proof = new Proof();
        proof.setDocument(new NationalNumber());

        ArgumentCaptor<String> proofKey = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ProofNotification> proofValue = ArgumentCaptor.forClass(ProofNotification.class);

        RMap proofNotifications = mock(RMap.class);
        when(redissonClient.getMap("proofs_" + number)).thenReturn(proofNotifications);
        when(proofNotifications.put(proofKey.capture(), proofValue.capture())).thenReturn(null);

        Notification result = notificationService.addProofNotification(user, proof, number);

        assertEquals(user.getNationalNumber() + Number.name(), proofKey.getValue());
        assertEquals(result, proofValue.getValue());
    }

    @Test
    void removeProofNotification_validInputs_notificationIsDeleted() {
        String nationalNumber = "number";
        String connectionNumber = "another number";

        ArgumentCaptor<String> connectionNumberCaptor = ArgumentCaptor.forClass(String.class);

        RMap proofNotifications = mock(RMap.class);
        when(redissonClient.getMap("proofs_" + nationalNumber)).thenReturn(proofNotifications);
        when(proofNotifications.remove(connectionNumberCaptor.capture())).thenReturn(null);

        notificationService.removeProofNotification(nationalNumber, connectionNumber, nationalNumber);

        assertEquals(connectionNumber + nationalNumber, connectionNumberCaptor.getValue());
    }

    @Test
    void addHealthNotification_validInputs_notificationIsReturned() {
        String number = "number";
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setFirstName("John");
        user.setPhoto("photo");
        user.setNationalNumber("another number");
        HeartHealthRecord record = new HeartHealthRecord();

        ArgumentCaptor<String> healthKey = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HealthNotification> healthValue = ArgumentCaptor.forClass(HealthNotification.class);

        RMap healthNotifications = mock(RMap.class);
        when(redissonClient.getMap("health_" + number)).thenReturn(healthNotifications);
        when(healthNotifications.put(healthKey.capture(), healthValue.capture())).thenReturn(null);

        Notification result = notificationService.addHealthNotification(user, record, number);

        assertEquals(user.getNationalNumber(), healthKey.getValue());
        assertEquals(result, healthValue.getValue());
    }

    @Test
    void removeHealthNotification_validInputs_notificationIsDeleted() {
        String nationalNumber = "number";
        String connectionNumber = "another number";

        ArgumentCaptor<String> connectionNumberCaptor = ArgumentCaptor.forClass(String.class);

        RMap healthNotifications = mock(RMap.class);
        when(redissonClient.getMap("health_" + nationalNumber)).thenReturn(healthNotifications);
        when(healthNotifications.remove(connectionNumberCaptor.capture())).thenReturn(null);

        notificationService.removeHealthNotification(nationalNumber, connectionNumber);

        assertEquals(connectionNumber, connectionNumberCaptor.getValue());
    }

    @Test
    void addOfferNotification_validInputs_notificationIsReturned() {

        String patientNumber = "number";

        Contact doctorContact = Contact.builder()
                .contactName("Doe John John")
                .nationalNumber("doctor's number")
                .photo("photo")
                .isVisible(true)
                .build();

        MedicalImage medicalImage = new MedicalImage();

        User doctor = new User();
        doctor.setFirstName("John");
        doctor.setLastName("Doe");
        doctor.setFatherName("John");
        doctor.setPhoto("photo");
        doctor.setNationalNumber("doctor's number");

        String patientPublicKey = "public";
        String patientPrivateKey = "private";

        ArgumentCaptor<String> credentialKey = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ProcessedUserCredentials> credentialValue = ArgumentCaptor.forClass(ProcessedUserCredentials.class);

        ArgumentCaptor<String> offersKey = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<OfferNotification> offersValue = ArgumentCaptor.forClass(OfferNotification.class);

        RMap credentials = mock(RMap.class);
        when(redissonClient.getMap("credentials")).thenReturn(credentials);
        when(credentials.put(credentialKey.capture(), credentialValue.capture())).thenReturn(null);

        RMap offerNotifications = mock(RMap.class);
        when(redissonClient.getMap("offers_" + patientNumber)).thenReturn(offerNotifications);
        when(offerNotifications.put(offersKey.capture(), offersValue.capture())).thenReturn(null);


        Notification result = notificationService
                .addOfferNotification(patientPublicKey, patientPrivateKey, doctor, medicalImage, patientNumber);
        result.setDate(null);

        Notification expected = new OfferNotification(null, doctorContact, OFFER_TITLE_EN, OFFER_TITLE_UKR,
                OFFER_CONTENT_EN, OFFER_CONTENT_UKR, medicalImage);

        assertEquals(expected, result);

        assertEquals(patientNumber + doctor.getNationalNumber(), credentialKey.getValue());

        assertEquals(ProcessedUserCredentials.builder()
                .publicKey(patientPublicKey)
                .privateKey(patientPrivateKey)
                .build(), credentialValue.getValue());

        assertEquals(patientPublicKey, offersKey.getValue());
    }

    @Test
    void removeOfferNotification_validInputs_credentialsAreReturned() {

        String doctorNumber = "doctor's number";
        String patientNumber = "patient's number";

        ProcessedUserCredentials userCredentials = ProcessedUserCredentials.builder()
                .publicKey("public")
                .privateKey("private")
                .build();

        ArgumentCaptor<String> getCredentialKey = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> removeCredentialKey = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> offersKey = ArgumentCaptor.forClass(String.class);

        RMap credentials = mock(RMap.class);
        when(redissonClient.getMap("credentials")).thenReturn(credentials);
        when(credentials.get(getCredentialKey.capture())).thenReturn(userCredentials);
        when(credentials.remove(removeCredentialKey.capture())).thenReturn(null);

        RMap patientNotifications = mock(RMap.class);
        when(redissonClient.getMap("offers_" + doctorNumber)).thenReturn(patientNotifications);
        when(patientNotifications.remove(offersKey.capture())).thenReturn(null);

        ProcessedUserCredentials result = notificationService.removeOfferNotification(doctorNumber, patientNumber);

        assertEquals(userCredentials, result);

        assertEquals(doctorNumber + patientNumber, getCredentialKey.getValue());

        assertEquals(doctorNumber + patientNumber, removeCredentialKey.getValue());

        assertEquals(userCredentials.getPublicKey(), offersKey.getValue());

    }


    @Test
    void addAskNotification_validInputs_notificationIsReturned() {
        String number = "number";
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setFirstName("John");
        user.setPhoto("photo");
        user.setNationalNumber("another number");

        ArgumentCaptor<String> askKey = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<AskNotification> askValue = ArgumentCaptor.forClass(AskNotification.class);

        RMap askNotifications = mock(RMap.class);
        when(redissonClient.getMap("asks_" + number)).thenReturn(askNotifications);
        when(askNotifications.put(askKey.capture(), askValue.capture())).thenReturn(null);

        Notification result = notificationService.addAskNotification(user, Collections.emptyList(), number);

        assertEquals(user.getNationalNumber(), askKey.getValue());
        assertEquals(result, askValue.getValue());
    }

    @Test
    void removeAskNotification_validInputs_notificationIsDeleted() {
        String nationalNumber = "number";
        String connectionNumber = "another number";

        ArgumentCaptor<String> connectionNumberCaptor = ArgumentCaptor.forClass(String.class);

        RMap askNotifications = mock(RMap.class);
        when(redissonClient.getMap("asks_" + nationalNumber)).thenReturn(askNotifications);
        when(askNotifications.remove(connectionNumberCaptor.capture())).thenReturn(null);

        notificationService.removeAskNotification(nationalNumber, connectionNumber);

        assertEquals(connectionNumber, connectionNumberCaptor.getValue());
    }

    @Test
    void addDocumentNotification_validInputs_notificationIsReturned() {
        String number = "number";
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setFirstName("John");
        user.setPhoto("photo");
        user.setNationalNumber("another number");
        MedicalImage medicalImage = new MedicalImage();

        ArgumentCaptor<String> documentKey = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<DocumentNotification> documentValue = ArgumentCaptor.forClass(DocumentNotification.class);

        RMap healthNotifications = mock(RMap.class);
        when(redissonClient.getMap("documents_" + number)).thenReturn(healthNotifications);
        when(healthNotifications.put(documentKey.capture(), documentValue.capture())).thenReturn(null);

        Notification result = notificationService.addDocumentNotification(user, medicalImage, number);

        assertEquals(user.getNationalNumber(), documentKey.getValue());
        assertEquals(result, documentValue.getValue());
    }

    @Test
    void removeDocumentNotification_validInputs_notificationIsDeleted() {
        String nationalNumber = "number";
        String connectionNumber = "another number";

        ArgumentCaptor<String> connectionNumberCaptor = ArgumentCaptor.forClass(String.class);

        RMap documentNotifications = mock(RMap.class);
        when(redissonClient.getMap("documents_" + nationalNumber)).thenReturn(documentNotifications);
        when(documentNotifications.remove(connectionNumberCaptor.capture())).thenReturn(null);

        notificationService.removeDocumentNotification(nationalNumber, connectionNumber);

        assertEquals(connectionNumber, connectionNumberCaptor.getValue());
    }

    @ParameterizedTest
    @MethodSource("provideNotifications")
    void removeNotification_validInputs_notificationIsRemoved(SystemNotification notification) {

        String nationalNumber = "number";

        notificationService = spy(notificationService);
        lenient().doReturn(new ProcessedUserCredentials()).when(notificationService).removeInviteNotification(anyString(), anyString());
        lenient().doNothing().when(notificationService).removePresentationNotification(anyString(), anyString(), anyString());
        lenient().doNothing().when(notificationService).removeProofNotification(anyString(), anyString(), anyString());
        lenient().doNothing().when(notificationService).removeHealthNotification(anyString(), anyString());
        lenient().doReturn(new ProcessedUserCredentials()).when(notificationService).removeOfferNotification(anyString(), anyString());
        lenient().doNothing().when(notificationService).removeAskNotification(anyString(), anyString());
        lenient().doNothing().when(notificationService).removeDocumentNotification(anyString(), anyString());

        notificationService.removeNotification(nationalNumber, notification);

        if (notification.getType().equals(INVITATION)) {
            verify(notificationService, times(1))
                    .removeInviteNotification(notification.getContact().getNationalNumber(), nationalNumber);
        }
        if (notification.getType().equals(CONNECTION)) {
            verify(notificationService, times(1))
                    .removeInviteNotification(nationalNumber, notification.getContact().getNationalNumber());
        }
        if (notification.getType().equals(PRESENTATION)) {
            verify(notificationService, times(1))
                    .removePresentationNotification(nationalNumber, notification.getContact().getNationalNumber(), Number.name());
        }
        if (notification.getType().equals(PROOF)) {
            verify(notificationService, times(1))
                    .removeProofNotification(nationalNumber, notification.getContact().getNationalNumber(), Number.name());
        }
        if (notification.getType().equals(HEALTH)) {
            verify(notificationService, times(1))
                    .removeHealthNotification(nationalNumber, notification.getContact().getNationalNumber());
        }
        if (notification.getType().equals(OFFER)) {
            verify(notificationService, times(1))
                    .removeOfferNotification(nationalNumber, notification.getContact().getNationalNumber());
        }
        if (notification.getType().equals(ASK)) {
            verify(notificationService, times(1))
                    .removeAskNotification(nationalNumber, notification.getContact().getNationalNumber());
        }
        if (notification.getType().equals(DOCUMENT)) {
            verify(notificationService, times(1))
                    .removeDocumentNotification(nationalNumber, notification.getContact().getNationalNumber());
        }
    }

    private static Stream<Arguments> provideNotifications() {

        Contact contact = Contact.builder().nationalNumber("contact number").build();

        return Stream.of(
                Arguments.of(new SystemNotification(null, INVITATION, contact, "INVITATION", "", "", "")),
                Arguments.of(new SystemNotification(null, CONNECTION, contact, "CONNECTION", "", "", "")),
                Arguments.of(new PresentationNotification(null, contact, "", "", "", "", Number.name(), Collections.emptyList())),
                Arguments.of(new ProofNotification(null, contact, "", "", "", "", new Proof("", "", "", "", new NationalNumber()))),
                Arguments.of(new HealthNotification(null, contact, "", "", "", "", new BodyMeasurement())),
                Arguments.of(new OfferNotification(null, contact, "", "", "", "", new MedicalImage())),
                Arguments.of(new AskNotification(null, contact, "", "", "", "", Collections.emptyList())),
                Arguments.of(new DocumentNotification(null, contact, "", "", "", "", new MedicalImage()))
        );
    }
}