package com.mokujin.user.model.notification.extention;

import com.mokujin.user.model.Contact;
import com.mokujin.user.model.document.Document;
import com.mokujin.user.model.notification.SystemNotification;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.mokujin.user.model.notification.Notification.Type.OFFER;

@Data
@EqualsAndHashCode(callSuper = true)
public class OfferNotification extends SystemNotification {

    private Document credential;

    public OfferNotification(Long date, Contact contact, String titleEn, String titleUkr, String contentEn,
                             String contentUkr, Document credential) {
        super(date, OFFER, contact, titleEn, titleUkr, contentEn, contentUkr);
        this.credential = credential;
    }
}
