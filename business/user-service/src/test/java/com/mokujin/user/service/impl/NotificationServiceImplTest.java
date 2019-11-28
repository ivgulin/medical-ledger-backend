package com.mokujin.user.service.impl;

import com.mokujin.user.model.Contact;
import com.mokujin.user.model.ProcessedUserCredentials;
import com.mokujin.user.model.User;
import com.mokujin.user.model.chat.Message;
import com.mokujin.user.model.document.impl.national.NationalNumber;
import com.mokujin.user.model.notification.Notification;
import com.mokujin.user.model.notification.NotificationCollector;
import com.mokujin.user.model.notification.SystemNotification;
import com.mokujin.user.model.notification.extention.ChatNotification;
import com.mokujin.user.model.notification.extention.PresentationNotification;
import com.mokujin.user.model.notification.extention.ProofNotification;
import com.mokujin.user.model.presentation.Proof;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import static com.mokujin.user.model.document.Document.NationalDocumentType.Number;
import static com.mokujin.user.model.document.Document.NationalDocumentType.Passport;
import static com.mokujin.user.model.notification.Notification.Type.CONNECTION;
import static com.mokujin.user.model.notification.Notification.Type.INVITATION;
import static com.mokujin.user.model.notification.NotificationConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private RedissonClient redissonClient;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    @Disabled
        // TODO: 23.11.19 get done when all notifications are described
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


        NotificationCollector expected = NotificationCollector.builder()
                .messages(messageNotifications)
                .connections(connectionNotifications)
                .invitations(invitationNotifications)
                .presentations(presentationNotifications)
                .proofs(proofNotifications)
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
}