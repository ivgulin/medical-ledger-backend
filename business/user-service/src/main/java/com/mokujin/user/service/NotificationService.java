package com.mokujin.user.service;

import com.mokujin.user.model.Contact;
import com.mokujin.user.model.ProcessedUserCredentials;
import com.mokujin.user.model.User;
import com.mokujin.user.model.chat.Message;
import com.mokujin.user.model.notification.Notification;
import com.mokujin.user.model.notification.NotificationCollector;

import java.util.List;

public interface NotificationService {

    NotificationCollector getNotifications(String nationalNumber);

    Notification addInviteNotification(String publicKey, String privateKey, Contact doctor, User user);

    ProcessedUserCredentials removeInviteNotification(String doctorNumber, String patientNumber);

    Notification addMessage(String connectionNumber, Message message);

    void removeMessage(String nationalNumber, Message message);

    Notification addPresentationNotification(User user, List<String> presentationAttributes,
                                             String documentType, String connectionNumber);
}
