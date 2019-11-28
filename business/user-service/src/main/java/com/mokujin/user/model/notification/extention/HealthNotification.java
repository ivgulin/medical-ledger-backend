package com.mokujin.user.model.notification.extention;

import com.mokujin.user.model.Contact;
import com.mokujin.user.model.notification.SystemNotification;
import com.mokujin.user.model.record.HealthRecord;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.mokujin.user.model.notification.Notification.Type.HEALTH;

@Data
@EqualsAndHashCode(callSuper = true)
public class HealthNotification extends SystemNotification {

    private HealthRecord record;

    public HealthNotification(Long date, Contact contact, String titleEn, String titleUkr, String contentEn,
                              String contentUkr, HealthRecord record) {
        super(date, HEALTH, contact, titleEn, titleUkr, contentEn, contentUkr);
        this.record = record;
    }
}
