package com.mokujin.user.service.impl;

import com.mokujin.user.model.Contact;
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
    public User inviteBack(String publicKey, String privateKey, Contact doctor) {

        User patient = userService.get(publicKey, privateKey);

        Notification notification = notificationService
                .addInviteNotification(publicKey, privateKey, doctor, patient);
        log.info("notification =  '{}'", notification);

        return patient;
    }

    @Override
    public User accept(String publicKey, String privateKey, String doctorNumber, String patientNumber) {

        ProcessedUserCredentials patientCredentials = notificationService
                .removeInviteNotification(doctorNumber, patientNumber);

        String url = "http://self-sovereign-identity-service/ledger/connect?public="
                + publicKey + "&private=" + privateKey;
        return restTemplate.postForObject(url, patientCredentials, User.class);
    }

    @Override
    public void decline(String doctorNumber, String patientNumber) {
        notificationService.removeInviteNotification(doctorNumber, patientNumber);
    }

}
