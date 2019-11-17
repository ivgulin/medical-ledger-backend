package com.mokujin.user.model.notification;

import com.mokujin.user.model.Contact;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class Notification {

    private Long date;

    private Contact contact;

    private Type type;

    enum Type {
        MESSAGE
    }

}
