package com.mokujin.user.service.impl;

import com.mokujin.user.model.Contact;
import com.mokujin.user.model.ProcessedUserCredentials;
import com.mokujin.user.model.User;
import com.mokujin.user.model.chat.Message;
import com.mokujin.user.model.notification.ChatNotification;
import com.mokujin.user.model.notification.Notification;
import com.mokujin.user.model.notification.NotificationCollector;
import com.mokujin.user.model.notification.SystemNotification;
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

import static com.mokujin.user.model.notification.Notification.Type.INVITATION;
import static com.mokujin.user.model.notification.NotificationContants.INVITATION_CONTENT_EN;
import static com.mokujin.user.model.notification.NotificationContants.INVITATION_CONTENT_UKR;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final RedissonClient redissonClient;

    @Override
    public NotificationCollector getNotifications(String nationalNumber) {

        RList<Message> messages = redissonClient.getList("messages_" + nationalNumber);
        List<ChatNotification> messageNotifications = messages.stream()
                .map(m -> new ChatNotification(new Date().getTime(), m))
                .collect(Collectors.toList());

        List<Notification> notifications = redissonClient.getList("notifications_" + nationalNumber).readAll()
                .stream()
                .filter(n -> n instanceof Notification)
                .map(n -> (SystemNotification) n)
                .collect(Collectors.toList());

        return NotificationCollector.builder()
                .messages(messageNotifications)
                .notifications(notifications)
                .build();
    }

    @Override
    public Notification addInviteNotification(String publicKey, String privateKey, String invitorNumber, User user) {
        ProcessedUserCredentials userCredentials = ProcessedUserCredentials.builder()
                .publicKey(publicKey)
                .privateKey(privateKey)
                .build();
        RMap<String, ProcessedUserCredentials> invitations = redissonClient.getMap("invitations");
        invitations.put(invitorNumber, userCredentials);

        RMap<String, Notification> notifications = redissonClient.getMap("notifications_" + invitorNumber);
        Notification notification = new SystemNotification(new Date().getTime(), INVITATION,
                Contact.builder()
                        .contactName(user.getFirstName() + " " + user.getFirstName() + " " + user.getFatherName())
                        .photo(user.getPhoto())
                        .nationalNumber(user.getNationalNumber())
                        .isVisible(true)
                        .build(), "", "", INVITATION_CONTENT_EN, INVITATION_CONTENT_UKR);
        notifications.put(publicKey, notification);

        return notification;
    }

    @Override
    public ProcessedUserCredentials removeInviteNotification(String invitorNumber) {
        RMap<String, ProcessedUserCredentials> invitations = redissonClient.getMap("invitations");
        ProcessedUserCredentials userCredentials = invitations.get(invitorNumber);
        invitations.remove(invitorNumber);

        RMap<String, Notification> notifications = redissonClient.getMap("notifications_" + invitorNumber);
        notifications.remove(userCredentials.getPublicKey());

        return userCredentials;
    }
}
