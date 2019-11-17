package com.mokujin.user.service.impl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.WebpushConfig;
import com.google.firebase.messaging.WebpushNotification;
import com.mokujin.user.model.Contact;
import com.mokujin.user.model.notification.ChatNotification;
import com.mokujin.user.model.notification.Notification;
import com.mokujin.user.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {


    @Override
    @SneakyThrows
    public Notification sendMessage(com.mokujin.user.model.chat.Message message,
                                    Contact contact, String notificationToken) {

        ChatNotification chatNotification = new ChatNotification(message.getDate(), contact, message.getContent());

        Message firebaseMessage = Message.builder()
                .setToken(notificationToken)
                .setWebpushConfig(WebpushConfig.builder()
                        .setNotification(WebpushNotification.builder()
                                .setTimestampMillis(chatNotification.getDate())
                                .setData(chatNotification)
                                .setBody(message.getContent())
                                .build())
                        .build())
                .build();

        FirebaseMessaging.getInstance().sendAsync(firebaseMessage).get();

        return chatNotification;
    }
}
