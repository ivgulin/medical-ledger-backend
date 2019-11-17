package com.mokujin.user.service;

import com.mokujin.user.model.ProcessedUserCredentials;
import com.mokujin.user.model.User;
import com.mokujin.user.model.notification.Notification;
import com.mokujin.user.model.notification.NotificationCollector;

public interface NotificationService {

    NotificationCollector getNotifications(String nationalNumber);

    Notification addInviteNotification(String publicKey, String privateKey, String invitorNumber, User user);

    ProcessedUserCredentials removeInviteNotification(String invitorNumber);

}
