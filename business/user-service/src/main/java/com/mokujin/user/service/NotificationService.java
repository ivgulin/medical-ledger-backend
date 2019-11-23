package com.mokujin.user.service;

import com.mokujin.user.model.Contact;
import com.mokujin.user.model.ProcessedUserCredentials;
import com.mokujin.user.model.User;
import com.mokujin.user.model.chat.Message;
import com.mokujin.user.model.notification.Notification;
import com.mokujin.user.model.notification.NotificationCollector;
import com.mokujin.user.model.presentation.Proof;
import com.mokujin.user.model.record.HealthRecord;

import java.util.List;

public interface NotificationService {

    NotificationCollector getNotifications(String nationalNumber);

    Notification addInviteNotification(String publicKey, String privateKey, Contact doctor, User user);

    ProcessedUserCredentials removeInviteNotification(String doctorNumber, String patientNumber);

    Notification addMessage(String connectionNumber, Message message);

    void removeMessage(String nationalNumber, Message message);

    Notification addPresentationNotification(User user, List<String> presentationAttributes,
                                             String documentType, String connectionNumber);

    void removePresentationNotification(User user, String connectionNumber);

    Notification addProofNotification(User user, Proof proof, String connectionNumber);

    void removeProofNotification(String nationalNumber, String connectionNumber);

    Notification addHealthNotification(User user, HealthRecord record, String connectionNumber);

    void removeHealthNotification(String nationalNumber, String connectionNumber);

}
