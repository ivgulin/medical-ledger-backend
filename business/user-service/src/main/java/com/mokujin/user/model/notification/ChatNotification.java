package com.mokujin.user.model.notification;

import com.mokujin.user.model.Contact;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChatNotification extends Notification {

    private String message;

    public ChatNotification(Long date, Contact contact, String message) {
        super(date, contact, Type.MESSAGE);
        this.message = message;
    }
}
