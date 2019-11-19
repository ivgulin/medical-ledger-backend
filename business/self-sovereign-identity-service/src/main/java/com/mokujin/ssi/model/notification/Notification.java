package com.mokujin.ssi.model.notification;

import com.mokujin.ssi.model.internal.Contact;
import lombok.Data;

@Data
public class Notification {

    private Long date;

    private Type type;

    private Contact contact;

    private String title;

    private String message;


    enum Type {
        OFFER,
        CONNECTION,
        PRESENTATION
    }
}
