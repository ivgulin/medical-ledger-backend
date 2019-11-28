package com.mokujin.user.model.notification.extention;

import com.mokujin.user.model.Contact;
import com.mokujin.user.model.document.Document;
import com.mokujin.user.model.notification.SystemNotification;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.mokujin.user.model.notification.Notification.Type.DOCUMENT;

@Data
@EqualsAndHashCode(callSuper = true)
public class DocumentNotification extends SystemNotification {

    private Document document;

    public DocumentNotification(Long date, Contact contact, String titleEn, String titleUkr, String contentEn,
                                String contentUkr, Document document) {
        super(date, DOCUMENT, contact, titleEn, titleUkr, contentEn, contentUkr);
        this.document = document;
    }
}
