package com.mokujin.user.model.notification;

import com.mokujin.user.model.chat.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChatNotification extends Notification {

    private Message message;

    public ChatNotification(Long date, Message message) {
        super(date, Type.MESSAGE);
        this.message = message;
    }
}
