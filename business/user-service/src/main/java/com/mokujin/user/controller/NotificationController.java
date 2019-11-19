package com.mokujin.user.controller;

import com.mokujin.user.model.notification.NotificationCollector;
import com.mokujin.user.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/get/{nationalNumber}")
    public ResponseEntity<NotificationCollector> get(@PathVariable String nationalNumber) {
        log.info("'get' invoked with params '{}'", nationalNumber);

        NotificationCollector notifications = notificationService.getNotifications(nationalNumber);

        log.info("'get' returned '{}'", notifications);
        return ResponseEntity.ok(notifications);
    }
}
