package com.mokujin.user.model.notification;

import com.mokujin.user.model.Contact;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SystemNotification extends Notification {

    private Contact contact;

    private String titleEn;

    private String titleUkr;

    private String contentEn;

    private String contentUkr;

    public SystemNotification(Long date, Type type, Contact contact, String titleEn,
                              String titleUkr, String contentEn, String contentUkr) {
        super(date, type);
        this.contact = contact;
        this.titleEn = titleEn;
        this.titleUkr = titleUkr;
        this.contentEn = contentEn;
        this.contentUkr = contentUkr;
    }
}
