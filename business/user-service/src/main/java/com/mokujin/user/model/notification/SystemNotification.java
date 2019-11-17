package com.mokujin.user.model.notification;

import com.mokujin.user.model.Contact;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SystemNotification extends Notification {
    private String titleEn;

    private String titleUkr;

    private String contentEn;

    private String contentUkr;

    public SystemNotification(Long date, Contact contact, Type type, String titleEn,
                              String titleUkr, String contentEn, String contentUkr) {
        super(date, contact, type);
        this.titleEn = titleEn;
        this.titleUkr = titleUkr;
        this.contentEn = contentEn;
        this.contentUkr = contentUkr;
    }
}
