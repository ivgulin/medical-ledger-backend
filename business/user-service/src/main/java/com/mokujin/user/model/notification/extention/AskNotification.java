package com.mokujin.user.model.notification.extention;

import com.mokujin.user.model.Contact;
import com.mokujin.user.model.notification.SystemNotification;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

import static com.mokujin.user.model.notification.Notification.Type.ASK;

@Data
@EqualsAndHashCode(callSuper = true)
public class AskNotification extends SystemNotification {

    private List<String> keywords;

    public AskNotification(Long date, Contact contact, String titleEn, String titleUkr,
                           String contentEn, String contentUkr, List<String> keywords) {
        super(date, ASK, contact, titleEn, titleUkr, contentEn, contentUkr);
        this.keywords = keywords;
    }
}
