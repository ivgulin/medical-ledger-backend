package com.mokujin.user.service.impl;

import com.mokujin.user.model.ProcessedUserCredentials;
import com.mokujin.user.model.User;
import com.mokujin.user.model.notification.Notification;
import com.mokujin.user.service.InvitationService;
import com.mokujin.user.service.NotificationService;
import com.mokujin.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvitationServiceImpl implements InvitationService {

    private final UserService userService;
    private final NotificationService notificationService;
    private final RestTemplate restTemplate;

    @Override
    public User inviteBack(String publicKey, String privateKey, String invitorNumber) {

        User user = userService.get(publicKey, privateKey);

        Notification notification = notificationService.addInviteNotification(publicKey, privateKey, invitorNumber, user);
        log.info("notification =  '{}'", notification);

        return user;
    }

    @Override
    public User accept(String publicKey, String privateKey, String nationalNumber) {

        ProcessedUserCredentials userCredentials = notificationService.removeInviteNotification(nationalNumber);

        String url = "http://self-sovereign-identity-service/ledger/connect?public="
                + publicKey + "&private=" + privateKey;
        return restTemplate.postForObject(url, userCredentials, User.class);
    }

    @Override
    public void decline(String nationalNumber) {
        notificationService.removeInviteNotification(nationalNumber);
    }

}
