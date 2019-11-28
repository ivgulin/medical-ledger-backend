package com.mokujin.user.service.impl;

import com.mokujin.user.model.User;
import com.mokujin.user.model.notification.Notification;
import com.mokujin.user.model.record.HealthRecord;
import com.mokujin.user.service.HealthDataService;
import com.mokujin.user.service.NotificationService;
import com.mokujin.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthDataServiceImpl implements HealthDataService {

    private final RestTemplate restTemplate;
    private final UserService userService;
    private final NotificationService notificationService;


    @Override
    public List<HealthRecord> save(String publicKey, String privateKey, HealthRecord healthRecord) {
        String url = "http://self-sovereign-identity-service/health/save" +
                "?public=" + publicKey +
                "&private=" + privateKey;
        return Arrays.asList(restTemplate.postForObject(url, healthRecord, HealthRecord[].class));
    }

    @Override
    public User share(String publicKey, String privateKey, HealthRecord healthRecord, String doctorNumber) {

        User patient = userService.get(publicKey, privateKey);

        Notification notification = notificationService.addHealthNotification(patient, healthRecord, doctorNumber);
        log.info("notification =  '{}'", notification);

        return patient;
    }
}
