package com.mokujin.user.service.impl;

import com.mokujin.user.model.Contact;
import com.mokujin.user.model.ProcessedUserCredentials;
import com.mokujin.user.model.User;
import com.mokujin.user.model.chat.Message;
import com.mokujin.user.model.notification.Notification;
import com.mokujin.user.model.notification.NotificationCollector;
import com.mokujin.user.model.notification.SystemNotification;
import com.mokujin.user.model.notification.extention.ChatNotification;
import com.mokujin.user.model.notification.extention.PresentationNotification;
import com.mokujin.user.model.notification.extention.ProofNotification;
import com.mokujin.user.model.presentation.Proof;
import com.mokujin.user.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.mokujin.user.model.notification.Notification.Type.*;
import static com.mokujin.user.model.notification.NotificationConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final RedissonClient redissonClient;

    @Override
    public NotificationCollector getNotifications(String nationalNumber) {

        RList<Message> messages = redissonClient.getList("messages_" + nationalNumber);
        List<ChatNotification> messageNotifications = messages.stream()
                .map(ChatNotification::new)
                .collect(Collectors.toList());

        List<SystemNotification> connectionNotifications = redissonClient.getMap("connections_" + nationalNumber)
                .values()
                .stream()
                .map(n -> (SystemNotification) n)
                .collect(Collectors.toList());

        List<SystemNotification> invitationNotifications = redissonClient.getMap("invitations_" + nationalNumber)
                .values()
                .stream()
                .map(n -> (SystemNotification) n)
                .collect(Collectors.toList());

        List<PresentationNotification> presentationNotifications = redissonClient.getMap("presentations_" + nationalNumber)
                .values()
                .stream()
                .map(n -> (PresentationNotification) n)
                .collect(Collectors.toList());

        List<ProofNotification> proofNotifications = redissonClient.getMap("proofs" + nationalNumber)
                .values()
                .stream()
                .map(n -> (ProofNotification) n)
                .collect(Collectors.toList());

        return NotificationCollector.builder()
                .messages(messageNotifications)
                .connections(connectionNotifications)
                .invitations(invitationNotifications)
                .presentations(presentationNotifications)
                .proofs(proofNotifications)
                .build();
    }

    @Override
    public Notification addInviteNotification(String publicKey, String privateKey, Contact doctor, User patient) {
        String patientNumber = patient.getNationalNumber();
        String doctorNumber = doctor.getNationalNumber();

        ProcessedUserCredentials patientCredentials = ProcessedUserCredentials.builder()
                .publicKey(publicKey)
                .privateKey(privateKey)
                .build();
        RMap<String, ProcessedUserCredentials> invitations = redissonClient.getMap("credentials");
        invitations.put(doctorNumber + patientNumber, patientCredentials);

        RMap<String, SystemNotification> doctorNotifications = redissonClient.getMap("connections_" + doctorNumber);
        SystemNotification connectionNotification = new SystemNotification(new Date().getTime(), CONNECTION,
                Contact.builder()
                        .contactName(patient.getFirstName() + " " + patient.getFirstName() + " " + patient.getFatherName())
                        .photo(patient.getPhoto())
                        .nationalNumber(patientNumber)
                        .isVisible(true)
                        .build(), "", "", CONNECTION_CONTENT_EN, CONNECTION_CONTENT_UKR);
        doctorNotifications.put(publicKey, connectionNotification);

        RMap<String, SystemNotification> patientNotifications = redissonClient.getMap("invitations_" + patientNumber);
        SystemNotification invitationNotification = new SystemNotification(new Date().getTime(), INVITATION,
                doctor, "", "", INVITATION_CONTENT_EN, INVITATION_CONTENT_UKR);
        patientNotifications.put(doctorNumber, invitationNotification);

        return invitationNotification;
    }

    @Override
    public ProcessedUserCredentials removeInviteNotification(String doctorNumber, String patientNumber) {
        RMap<String, ProcessedUserCredentials> invitations = redissonClient.getMap("credentials");
        ProcessedUserCredentials patientCredentials = invitations.get(doctorNumber + patientNumber);
        invitations.remove(doctorNumber + patientNumber);

        RMap<String, SystemNotification> doctorNotifications = redissonClient.getMap("connections_" + doctorNumber);
        doctorNotifications.remove(patientCredentials.getPublicKey());

        RMap<String, SystemNotification> patientNotifications = redissonClient.getMap("invitations_" + patientNumber);
        patientNotifications.remove(doctorNumber);

        return patientCredentials;
    }

    @Override
    public Notification addMessage(String connectionNumber, Message message) {
        RList<Message> messages = redissonClient.getList("messages_" + connectionNumber);
        messages.add(message);

        return new ChatNotification(message);
    }

    @Override
    public void removeMessage(String nationalNumber, Message message) {
        RList<Message> messages = redissonClient.getList("messages_" + nationalNumber);
        messages.remove(message);
    }

    @Override
    public Notification addPresentationNotification(User user, List<String> presentationAttributes,
                                                    String documentType, String connectionNumber) {

        RMap<String, PresentationNotification> presentationNotifications = redissonClient.getMap("presentations_" + connectionNumber);
        String nationalNumber = user.getNationalNumber();
        PresentationNotification presentationNotification = new PresentationNotification(new Date().getTime(),
                PRESENTATION,
                Contact.builder()
                        .contactName(user.getFirstName() + " " + user.getFirstName() + " " + user.getFatherName())
                        .photo(user.getPhoto())
                        .nationalNumber(nationalNumber)
                        .isVisible(true)
                        .build(), PRESENTATION_TITLE_EN, PRESENTATION_TITLE_UKR, PRESENTATION_CONTENT_EN,
                PRESENTATION_CONTENT_UKR, documentType, presentationAttributes);
        presentationNotifications.put(nationalNumber, presentationNotification);

        return presentationNotification;
    }

    @Override
    public void removePresentationNotification(User user, String connectionNumber) {
        String nationalNumber = user.getNationalNumber();
        RMap<String, PresentationNotification> presentationNotifications = redissonClient.getMap("presentations_" + nationalNumber);
        presentationNotifications.remove(connectionNumber);
    }

    @Override
    public Notification addProofNotification(User user, Proof proof, String connectionNumber) {
        RMap<String, ProofNotification> proofNotifications = redissonClient.getMap("proofs_" + connectionNumber);
        String nationalNumber = user.getNationalNumber();
        ProofNotification proofNotification = new ProofNotification(new Date().getTime(),
                PROOF,
                Contact.builder()
                        .contactName(user.getFirstName() + " " + user.getFirstName() + " " + user.getFatherName())
                        .photo(user.getPhoto())
                        .nationalNumber(nationalNumber)
                        .isVisible(true)
                        .build(), PROOF_TITLE_EN, PROOF_TITLE_UKR, PROOF_CONTENT_EN, PROOF_CONTENT_UKR, proof);
        proofNotifications.put(nationalNumber, proofNotification);

        return proofNotification;
    }

    @Override
    public void removeProofNotification(String nationalNumber, String connectionNumber) {
        RMap<String, ProofNotification> proofNotifications = redissonClient.getMap("proofs_" + nationalNumber);
        proofNotifications.remove(connectionNumber);
    }
}
