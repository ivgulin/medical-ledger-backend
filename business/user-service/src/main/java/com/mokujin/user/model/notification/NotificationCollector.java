package com.mokujin.user.model.notification;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class NotificationCollector {

    private List<Notification> notifications = new ArrayList<>();

    private List<ChatNotification> messages = new ArrayList<>();

}
