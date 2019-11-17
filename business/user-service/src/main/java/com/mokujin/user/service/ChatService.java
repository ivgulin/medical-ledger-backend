package com.mokujin.user.service;

import com.mokujin.user.model.Contact;
import com.mokujin.user.model.chat.Chat;
import com.mokujin.user.model.chat.Message;

public interface ChatService {

    Chat get(String publicKey, String privateKey, String connectionNumber,
             String userNumber, String notificationToken);

    Chat addMessage(String publicKey, String privateKey, String connectionNumber, Message message);

    Chat addMessageWithNotification(String publicKey, String privateKey, Contact contact, Message message);
}
