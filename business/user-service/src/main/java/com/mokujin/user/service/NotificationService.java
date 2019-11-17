package com.mokujin.user.service;

import com.mokujin.user.model.Contact;
import com.mokujin.user.model.chat.Message;
import com.mokujin.user.model.notification.Notification;

public interface NotificationService {

    Notification sendMessage(Message message, Contact contact, String notificationToken);

}
