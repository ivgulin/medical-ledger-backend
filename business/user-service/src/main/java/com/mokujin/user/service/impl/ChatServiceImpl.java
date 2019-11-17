package com.mokujin.user.service.impl;

import com.mokujin.user.model.Contact;
import com.mokujin.user.model.chat.Chat;
import com.mokujin.user.model.chat.Message;
import com.mokujin.user.model.notification.Notification;
import com.mokujin.user.service.ChatService;
import com.mokujin.user.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final RestTemplate restTemplate;
    private final RedissonClient redissonClient;
    private final NotificationService notificationService;

    @Override
    @SneakyThrows
    public Chat get(String publicKey, String privateKey, String connectionNumber,
                    String userNumber, String notificationToken) {

        RMap<Object, Object> notificationTokens = redissonClient.getMap("notifications");
        notificationTokens.put(userNumber, notificationToken);
        String connectionNotificationToken = (String) notificationTokens.get(connectionNumber);

        String url = "http://self-sovereign-identity-service/chat/get/" + connectionNumber +
                "?token=" + connectionNotificationToken +
                "&public=" + publicKey +
                "&private=" + privateKey;
        return restTemplate.getForObject(url, Chat.class);
    }

    @Override
    public Chat addMessage(String publicKey, String privateKey, String connectionNumber, Message message) {

        RMap<Object, Object> notificationTokens = redissonClient.getMap("notifications");
        String connectionNotificationToken = (String) notificationTokens.get(connectionNumber);
        String url = "http://self-sovereign-identity-service/chat/add/" + connectionNumber +
                "?token=" + connectionNotificationToken +
                "&public=" + publicKey +
                "&private=" + privateKey;
        return restTemplate.postForObject(url, message, Chat.class);
    }

    @Override
    public Chat addMessageWithNotification(String publicKey, String privateKey, Contact contact, Message message) {

        RMap<Object, Object> notificationTokens = redissonClient.getMap("notifications");
        String connectionNotificationToken = (String) notificationTokens.get(contact.getNationalNumber());

        Notification notification = notificationService.sendMessage(message, contact, connectionNotificationToken);
        log.info("notification =  '{}'", notification);

        String url = "http://self-sovereign-identity-service/chat/add/" + contact.getNationalNumber() +
                "?token=" + connectionNotificationToken +
                "&public=" + publicKey +
                "&private=" + privateKey;
        return restTemplate.postForObject(url, message, Chat.class);
    }
}
