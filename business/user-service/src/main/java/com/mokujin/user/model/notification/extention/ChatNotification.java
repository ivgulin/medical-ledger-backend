package com.mokujin.user.model.notification.extention;

import com.mokujin.user.model.chat.Message;
import com.mokujin.user.model.notification.Notification;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.mokujin.user.model.notification.Notification.Type.*;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChatNotification extends Notification {

    private Message message;

    public ChatNotification(Message message) {
        super(message.getDate(), MESSAGE);
        this.message = message;
    }
}
